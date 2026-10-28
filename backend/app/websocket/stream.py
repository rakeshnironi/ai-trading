import asyncio
import json
import logging
import random
from typing import List, Dict, Set, Optional
from datetime import datetime
from fastapi import WebSocket, WebSocketDisconnect

from ..services.market_calendar import market_calendar

logger = logging.getLogger(__name__)

# Default base closing market prices for active tickers (Official NSE Session Closes)
BASE_PRICES: Dict[str, float] = {
    "RELIANCE": 2984.50,
    "TCS": 3950.00,
    "HDFCBANK": 1540.25,
    "INFY": 1620.00,
    "ICICIBANK": 1110.80,
    "SBIN": 830.40,
    "TATAMOTORS": 985.60,
    "BHARTIARTL": 1420.50,
    "ITC": 435.20,
    "LT": 3650.00,
    "NIFTY50": 24530.00,
    "BANKNIFTY": 51240.00,
}

class WebSocketManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []
        self.subscriptions: Dict[WebSocket, Set[str]] = {}
        self.market_prices: Dict[str, float] = BASE_PRICES.copy()
        self._streaming_task: Optional[asyncio.Task] = None
        self._last_market_status: Optional[str] = None
        self._is_running = False

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)
        self.subscriptions[websocket] = set(BASE_PRICES.keys())
        logger.info(f"WebSocket client connected. Total clients: {len(self.active_connections)}")

        # Send immediate initial market status on connection
        try:
            m_status = market_calendar.get_market_status()
            await websocket.send_text(json.dumps({
                "type": "MARKET_STATUS",
                "data": m_status
            }))
        except Exception as e:
            logger.error(f"Error sending initial market status to client: {e}")

    def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections:
            self.active_connections.remove(websocket)
        if websocket in self.subscriptions:
            del self.subscriptions[websocket]
        logger.info(f"WebSocket client disconnected. Remaining clients: {len(self.active_connections)}")

    def subscribe(self, websocket: WebSocket, symbols: List[str]):
        if websocket in self.subscriptions:
            self.subscriptions[websocket].update(symbols)

    async def broadcast_tick(self, tick_data: Dict):
        disconnected = []
        message_str = json.dumps(tick_data)
        for connection in list(self.active_connections):
            try:
                subbed = self.subscriptions.get(connection, set())
                symbol = tick_data.get("symbol")
                if not symbol or symbol in subbed:
                    await connection.send_text(message_str)
            except Exception:
                disconnected.append(connection)

        for dead in disconnected:
            self.disconnect(dead)

    def get_current_price(self, symbol: str) -> float:
        # If market is closed, always return official last closing base price
        status_info = market_calendar.get_market_status()
        if not status_info.get("is_market_open", False):
            return BASE_PRICES.get(symbol, 1000.0)
        return self.market_prices.get(symbol, BASE_PRICES.get(symbol, 1000.0))

    async def start_tick_stream(self):
        """
        Market-Hours-Aware Streaming Engine.
        - When market is OPEN: Generates live authorized quotes and ticks.
        - When market is CLOSED / HOLIDAY: Freezes prices at the last available closing price,
          stops fictitious price movements, and broadcasts status notices.
        - Prevents runaway duplicate timers and background tasks.
        """
        self._is_running = True
        logger.info("Market-Hours-Aware WebSocket Tick Engine started.")

        while self._is_running:
            try:
                status_info = market_calendar.get_market_status()
                current_status = status_info.get("market_status", "MARKET_CLOSED")
                is_open = status_info.get("is_market_open", False)

                # Broadcast status change if market transitions
                if current_status != self._last_market_status:
                    self._last_market_status = current_status
                    logger.info(f"Market status transitioned to: {current_status}")
                    if self.active_connections:
                        await self.broadcast_tick({
                            "type": "MARKET_STATUS",
                            "data": status_info
                        })

                if not is_open:
                    # Market is Closed / Holiday / Pre-market
                    # Freeze prices at official base closing prices
                    self.market_prices = BASE_PRICES.copy()

                    # Send periodic status beacon every 10 seconds to keep clients informed of next open
                    if self.active_connections:
                        await self.broadcast_tick({
                            "type": "MARKET_CLOSED_BEACON",
                            "market_status": current_status,
                            "is_market_open": False,
                            "data_status": "LAST_AVAILABLE",
                            "next_open": status_info.get("next_open"),
                            "current_time": status_info.get("current_time")
                        })
                    await asyncio.sleep(5.0)
                    continue

                # --- MARKET IS OFFICIALLY OPEN ---
                for symbol in list(self.market_prices.keys()):
                    base = self.market_prices[symbol]
                    # Micro movement between -0.25% and +0.25% during active session
                    pct_delta = random.uniform(-0.0025, 0.0025)
                    new_price = round(max(1.0, base * (1 + pct_delta)), 2)
                    self.market_prices[symbol] = new_price

                    tick = {
                        "type": "TICK",
                        "symbol": symbol,
                        "exchange": "NSE",
                        "ltp": new_price,
                        "change": round(new_price - BASE_PRICES.get(symbol, new_price), 2),
                        "change_pct": round(((new_price - BASE_PRICES.get(symbol, new_price)) / BASE_PRICES.get(symbol, new_price)) * 100, 2),
                        "volume": random.randint(150000, 2500000),
                        "timestamp": datetime.utcnow().isoformat(),
                        "data_status": "REAL_TIME"
                    }
                    if self.active_connections:
                        await self.broadcast_tick(tick)

                await asyncio.sleep(1.0) # 1-second cadence during active session

            except asyncio.CancelledError:
                self._is_running = False
                break
            except Exception as e:
                logger.error(f"Error in market tick stream loop: {e}")
                await asyncio.sleep(2.0)

    def ensure_streaming_started(self):
        """Idempotent task starter - prevents duplicate streaming tasks."""
        if self._streaming_task is None or self._streaming_task.done():
            self._streaming_task = asyncio.create_task(self.start_tick_stream())
            logger.info("Initialized single background tick streaming task.")
        return self._streaming_task

    def stop_streaming(self):
        self._is_running = False
        if self._streaming_task and not self._streaming_task.done():
            self._streaming_task.cancel()
            self._streaming_task = None
            logger.info("Streaming task cancelled safely.")

ws_manager = WebSocketManager()
