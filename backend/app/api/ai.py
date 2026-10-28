from typing import Optional
from fastapi import APIRouter, Query
from ..ai.model import AIPredictionEngine
from ..indicators.technical import TechnicalAnalysis
from ..api.market import get_candles
from ..models.schemas import AIPredictionResponse, TechnicalIndicatorsResponse

router = APIRouter(prefix="/ai", tags=["AI & Technicals"])

@router.get("/predict/{symbol}", response_model=AIPredictionResponse)
def get_ai_prediction(
    symbol: str,
    timeframe: str = Query("1D", pattern="^(1m|5m|15m|1h|1D)$")
):
    """
    Computes real-time multi-factor ML consensus for a symbol using OHLCV series.
    Returns BUY, SELL, or HOLD with confidence score, target, stop loss, and rationale.
    """
    candles = get_candles(symbol=symbol, timeframe=timeframe, limit=50)
    ohlcv_data = [
        {
            "open": c.open,
            "high": c.high,
            "low": c.low,
            "close": c.close,
            "volume": c.volume
        }
        for c in candles
    ]

    prediction = AIPredictionEngine.predict(symbol=symbol.upper(), ohlcv=ohlcv_data)
    return prediction

@router.get("/indicators/{symbol}", response_model=TechnicalIndicatorsResponse)
def get_technical_indicators(
    symbol: str,
    timeframe: str = Query("1D", pattern="^(1m|5m|15m|1h|1D)$")
):
    """
    Calculates technical indicators: RSI, EMA20, EMA50, MACD, VWAP, ATR.
    """
    candles = get_candles(symbol=symbol, timeframe=timeframe, limit=50)
    ohlcv_data = [
        {
            "open": c.open,
            "high": c.high,
            "low": c.low,
            "close": c.close,
            "volume": c.volume
        }
        for c in candles
    ]
    indicators = TechnicalAnalysis.compute_all(ohlcv_data)
    return TechnicalIndicatorsResponse(
        symbol=symbol.upper(),
        **indicators
    )
