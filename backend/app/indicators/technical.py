import math
from typing import List, Dict, Any, Tuple
from datetime import datetime

class TechnicalAnalysis:
    @staticmethod
    def calculate_ema(prices: List[float], period: int) -> List[float]:
        """Exponential Moving Average (EMA)."""
        if not prices:
            return []
        if len(prices) < period:
            return [sum(prices) / len(prices)] * len(prices)

        multiplier = 2.0 / (period + 1)
        ema_values = [sum(prices[:period]) / period]

        for price in prices[period:]:
            new_ema = (price - ema_values[-1]) * multiplier + ema_values[-1]
            ema_values.append(new_ema)

        # Pad front
        padding = [ema_values[0]] * (len(prices) - len(ema_values))
        return padding + ema_values

    @staticmethod
    def calculate_rsi(prices: List[float], period: int = 14) -> float:
        """Relative Strength Index (RSI) using Wilder's smoothing."""
        if len(prices) < period + 1:
            return 50.0

        gains = []
        losses = []
        for i in range(1, len(prices)):
            diff = prices[i] - prices[i - 1]
            if diff >= 0:
                gains.append(diff)
                losses.append(0.0)
            else:
                gains.append(0.0)
                losses.append(abs(diff))

        if len(gains) < period:
            return 50.0

        avg_gain = sum(gains[:period]) / period
        avg_loss = sum(losses[:period]) / period

        for i in range(period, len(gains)):
            avg_gain = (avg_gain * (period - 1) + gains[i]) / period
            avg_loss = (avg_loss * (period - 1) + losses[i]) / period

        if avg_loss == 0.0:
            return 100.0
        rs = avg_gain / avg_loss
        rsi = 100.0 - (100.0 / (1.0 + rs))
        return round(rsi, 2)

    @staticmethod
    def calculate_macd(prices: List[float], fast: int = 12, slow: int = 26, signal_period: int = 9) -> Tuple[float, float, float]:
        """MACD Line, Signal Line, and Histogram."""
        if len(prices) < slow:
            return 0.0, 0.0, 0.0

        ema_fast = TechnicalAnalysis.calculate_ema(prices, fast)
        ema_slow = TechnicalAnalysis.calculate_ema(prices, slow)

        macd_line = [f - s for f, s in zip(ema_fast, ema_slow)]
        signal_line = TechnicalAnalysis.calculate_ema(macd_line, signal_period)

        current_macd = macd_line[-1]
        current_signal = signal_line[-1]
        current_hist = current_macd - current_signal

        return round(current_macd, 2), round(current_signal, 2), round(current_hist, 2)

    @staticmethod
    def calculate_vwap(highs: List[float], lows: List[float], closes: List[float], volumes: List[float]) -> float:
        """Volume Weighted Average Price (VWAP)."""
        if not closes or not volumes:
            return closes[-1] if closes else 0.0

        cum_tp_vol = 0.0
        cum_vol = 0.0

        for h, l, c, v in zip(highs, lows, closes, volumes):
            tp = (h + l + c) / 3.0
            cum_tp_vol += tp * v
            cum_vol += v

        if cum_vol == 0:
            return closes[-1]
        return round(cum_tp_vol / cum_vol, 2)

    @staticmethod
    def calculate_atr(highs: List[float], lows: List[float], closes: List[float], period: int = 14) -> float:
        """Average True Range (ATR)."""
        if len(closes) < 2:
            return 10.0

        true_ranges = []
        for i in range(1, len(closes)):
            tr = max(
                highs[i] - lows[i],
                abs(highs[i] - closes[i - 1]),
                abs(lows[i] - closes[i - 1])
            )
            true_ranges.append(tr)

        if not true_ranges:
            return 10.0
        if len(true_ranges) < period:
            return round(sum(true_ranges) / len(true_ranges), 2)

        return round(sum(true_ranges[-period:]) / period, 2)

    @classmethod
    def compute_all(cls, ohlcv: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Compute full suite of technical indicators from candle list."""
        if not ohlcv:
            return {
                "rsi": 50.0,
                "ema20": 0.0,
                "ema50": 0.0,
                "macd": 0.0,
                "macd_signal": 0.0,
                "macd_hist": 0.0,
                "vwap": 0.0,
                "atr": 10.0,
                "signal_bias": "NEUTRAL"
            }

        closes = [c["close"] for c in ohlcv]
        highs = [c["high"] for c in ohlcv]
        lows = [c["low"] for c in ohlcv]
        volumes = [c.get("volume", 1000.0) for c in ohlcv]

        rsi = cls.calculate_rsi(closes)
        ema20_series = cls.calculate_ema(closes, 20)
        ema50_series = cls.calculate_ema(closes, 50)
        ema20 = round(ema20_series[-1], 2) if ema20_series else closes[-1]
        ema50 = round(ema50_series[-1], 2) if ema50_series else closes[-1]

        macd, macd_sig, macd_hist = cls.calculate_macd(closes)
        vwap = cls.calculate_vwap(highs, lows, closes, volumes)
        atr = cls.calculate_atr(highs, lows, closes)

        # Signal bias determination
        bullish_score = 0
        bearish_score = 0

        if rsi < 35:
            bullish_score += 2 # Oversold
        elif rsi > 65:
            bearish_score += 2 # Overbought

        if closes[-1] > ema20 > ema50:
            bullish_score += 2
        elif closes[-1] < ema20 < ema50:
            bearish_score += 2

        if macd > macd_sig and macd_hist > 0:
            bullish_score += 2
        elif macd < macd_sig and macd_hist < 0:
            bearish_score += 2

        if closes[-1] > vwap:
            bullish_score += 1
        else:
            bearish_score += 1

        if bullish_score > bearish_score + 1:
            bias = "BULLISH"
        elif bearish_score > bullish_score + 1:
            bias = "BEARISH"
        else:
            bias = "NEUTRAL"

        return {
            "rsi": rsi,
            "ema20": ema20,
            "ema50": ema50,
            "macd": macd,
            "macd_signal": macd_sig,
            "macd_hist": macd_hist,
            "vwap": vwap,
            "atr": atr,
            "signal_bias": bias
        }
