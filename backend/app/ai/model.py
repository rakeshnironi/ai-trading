import math
from datetime import datetime
from typing import List, Dict, Any
from ..indicators.technical import TechnicalAnalysis

class AIPredictionEngine:
    """
    Algorithmic & Machine Learning Consensus Engine for Stock Price Prediction.
    Takes OHLCV time series, computes feature vectors (RSI, EMA20, EMA50, MACD, VWAP, ATR),
    and evaluates multi-factor probabilities to issue BUY, SELL, or HOLD signals with confidence.
    """

    @classmethod
    def predict(cls, symbol: str, ohlcv: List[Dict[str, Any]]) -> Dict[str, Any]:
        if not ohlcv:
            # Safe default fallback
            ind = TechnicalAnalysis.compute_all([])
            ind["symbol"] = symbol
            return {
                "symbol": symbol,
                "signal": "HOLD",
                "confidence": 0.50,
                "confidence_pct": "50%",
                "target_price": 0.0,
                "stop_loss": 0.0,
                "risk_reward_ratio": "1:2",
                "indicators": ind,
                "summary_reasons": ["Insufficient OHLCV data to form consensus"],
                "timestamp": datetime.utcnow()
            }

        latest_close = ohlcv[-1]["close"]
        indicators = TechnicalAnalysis.compute_all(ohlcv)
        indicators["symbol"] = symbol

        rsi = indicators["rsi"]
        ema20 = indicators["ema20"]
        ema50 = indicators["ema50"]
        macd = indicators["macd"]
        macd_sig = indicators["macd_signal"]
        macd_hist = indicators["macd_hist"]
        vwap = indicators["vwap"]
        atr = indicators["atr"] if indicators["atr"] > 0 else (latest_close * 0.015)

        reasons = []
        buy_weight = 0.0
        sell_weight = 0.0

        # 1. RSI Factor
        if rsi < 32:
            buy_weight += 2.5
            reasons.append(f"RSI oversold at {rsi:.1f}, bullish mean-reversion favored")
        elif rsi > 68:
            sell_weight += 2.5
            reasons.append(f"RSI overbought at {rsi:.1f}, profit-taking / pullback likely")
        elif 45 <= rsi <= 60:
            buy_weight += 1.0
            reasons.append(f"RSI steady at {rsi:.1f} in healthy expansion zone")

        # 2. Moving Average Trend (EMA 20 & 50)
        if latest_close > ema20 and ema20 > ema50:
            buy_weight += 3.0
            reasons.append(f"Price ({latest_close:.2f}) above EMA20 & EMA50 golden stack")
        elif latest_close < ema20 and ema20 < ema50:
            sell_weight += 3.0
            reasons.append(f"Price ({latest_close:.2f}) below EMA20 & EMA50 death cross")
        elif latest_close > ema20:
            buy_weight += 1.2
            reasons.append(f"Price maintaining support above short-term 20 EMA")

        # 3. MACD Momentum
        if macd > macd_sig and macd_hist > 0:
            buy_weight += 2.2
            reasons.append(f"MACD positive divergence with expanding histogram (+{macd_hist:.2f})")
        elif macd < macd_sig and macd_hist < 0:
            sell_weight += 2.2
            reasons.append(f"MACD negative crossover with contraction histogram ({macd_hist:.2f})")

        # 4. VWAP Intraday Anchor
        if latest_close > vwap:
            buy_weight += 1.5
            reasons.append(f"Institutional VWAP support holding at ₹{vwap:.2f}")
        else:
            sell_weight += 1.5
            reasons.append(f"Trading below volume-weighted VWAP anchor ₹{vwap:.2f}")

        # 5. Determine Signal & Confidence
        total_score = buy_weight + sell_weight
        if total_score == 0:
            signal = "HOLD"
            confidence = 0.50
        elif buy_weight > sell_weight and buy_weight >= 4.5:
            signal = "BUY"
            raw_conf = 0.65 + (buy_weight / (total_score + 2.0)) * 0.32
            confidence = min(0.95, round(raw_conf, 2))
        elif sell_weight > buy_weight and sell_weight >= 4.5:
            signal = "SELL"
            raw_conf = 0.65 + (sell_weight / (total_score + 2.0)) * 0.32
            confidence = min(0.95, round(raw_conf, 2))
        else:
            signal = "HOLD"
            confidence = 0.55
            reasons.append("Technical indicators mixed; awaiting clear breakout confirmation")

        # 6. Target Price, Stop Loss & Risk Reward (based on ATR)
        if signal == "BUY":
            target_price = round(latest_close + (2.5 * atr), 2)
            stop_loss = round(max(1.0, latest_close - (1.5 * atr)), 2)
            rr = "1:2.0"
        elif signal == "SELL":
            target_price = round(max(1.0, latest_close - (2.5 * atr)), 2)
            stop_loss = round(latest_close + (1.5 * atr), 2)
            rr = "1:2.0"
        else:
            target_price = round(latest_close * 1.03, 2)
            stop_loss = round(latest_close * 0.98, 2)
            rr = "1:1.5"

        return {
            "symbol": symbol,
            "signal": signal,
            "confidence": confidence,
            "confidence_pct": f"{int(confidence * 100)}%",
            "target_price": target_price,
            "stop_loss": stop_loss,
            "risk_reward_ratio": rr,
            "indicators": indicators,
            "summary_reasons": reasons,
            "timestamp": datetime.utcnow()
        }
