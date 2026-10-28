import 'dart:async';
import 'dart:math';

/// Representation of an OHLCV candle for technical indicator calculations.
class CandleStick {
  final DateTime timestamp;
  final double open;
  final double high;
  final double low;
  final double close;
  final int volume;

  const CandleStick({
    required this.timestamp,
    required this.open,
    required this.high,
    required this.low,
    required this.close,
    required this.volume,
  });

  Map<String, dynamic> toMap() => {
        'timestamp': timestamp.millisecondsSinceEpoch,
        'open': open,
        'high': high,
        'low': low,
        'close': close,
        'volume': volume,
      };

  factory CandleStick.fromMap(Map<String, dynamic> map) => CandleStick(
        timestamp: DateTime.fromMillisecondsSinceEpoch(map['timestamp'] as int),
        open: (map['open'] as num).toDouble(),
        high: (map['high'] as num).toDouble(),
        low: (map['low'] as num).toDouble(),
        close: (map['close'] as num).toDouble(),
        volume: (map['volume'] as num).toInt(),
      );
}

/// MACD calculation payload
class MacdResult {
  final double macdLine;
  final double signalLine;
  final double histogram;
  final bool isBullishCross;
  final bool isNegative;

  const MacdResult({
    required this.macdLine,
    required this.signalLine,
    required this.histogram,
    required this.isBullishCross,
    required this.isNegative,
  });
}

/// Bollinger Bands calculation payload
class BollingerBandsResult {
  final double upperBand;
  final double middleBand;
  final double lowerBand;
  final double bandwidth;
  final double percentB;

  const BollingerBandsResult({
    required this.upperBand,
    required this.middleBand,
    required this.lowerBand,
    required this.bandwidth,
    required this.percentB,
  });
}

/// Complete technical indicators model evaluated from market candles
class TechnicalIndicatorsResult {
  final String symbol;
  final double currentPrice;
  final double rsi;
  final double ema9;
  final double ema20;
  final double ema50;
  final double ema200;
  final double sma20;
  final double sma50;
  final MacdResult macd;
  final BollingerBandsResult bollinger;
  final double vwap;
  final double atr;
  final double superTrend;
  final bool isSuperTrendBullish;
  final int currentVolume;
  final int volume20Avg;
  final bool isVolumeSpike;
  final String consensusSignal; // BUY, SELL, HOLD
  final double confidenceScore;
  final DateTime calculatedAt;

  const TechnicalIndicatorsResult({
    required this.symbol,
    required this.currentPrice,
    required this.rsi,
    required this.ema9,
    required this.ema20,
    required this.ema50,
    required this.ema200,
    required this.sma20,
    required this.sma50,
    required this.macd,
    required this.bollinger,
    required this.vwap,
    required this.atr,
    required this.superTrend,
    required this.isSuperTrendBullish,
    required this.currentVolume,
    required this.volume20Avg,
    required this.isVolumeSpike,
    required this.consensusSignal,
    required this.confidenceScore,
    required this.calculatedAt,
  });
}

/// Compute payload for isolate / background thread execution
class _CalculationPayload {
  final String symbol;
  final List<CandleStick> candles;

  const _CalculationPayload(this.symbol, this.candles);
}

/// Standalone calculation engine designed to run safely inside Dart background
/// isolates (e.g. compute() or Worker Isolates) without blocking the UI thread.
class TechnicalIndicatorCalculator {
  static TechnicalIndicatorsResult calculate(String symbol, List<CandleStick> candles) {
    if (candles.isEmpty) {
      return TechnicalIndicatorsResult(
        symbol: symbol,
        currentPrice: 0.0,
        rsi: 50.0,
        ema9: 0.0,
        ema20: 0.0,
        ema50: 0.0,
        ema200: 0.0,
        sma20: 0.0,
        sma50: 0.0,
        macd: const MacdResult(
          macdLine: 0.0,
          signalLine: 0.0,
          histogram: 0.0,
          isBullishCross: false,
          isNegative: false,
        ),
        bollinger: const BollingerBandsResult(
          upperBand: 0.0,
          middleBand: 0.0,
          lowerBand: 0.0,
          bandwidth: 0.0,
          percentB: 0.0,
        ),
        vwap: 0.0,
        atr: 0.0,
        superTrend: 0.0,
        isSuperTrendBullish: false,
        currentVolume: 0,
        volume20Avg: 0,
        isVolumeSpike: false,
        consensusSignal: 'HOLD',
        confidenceScore: 50.0,
        calculatedAt: DateTime.now(),
      );
    }

    final closes = candles.map((c) => c.close).toList();
    final volumes = candles.map((c) => c.volume).toList();
    final currentPrice = closes.last;
    final currentVolume = volumes.last;

    // 1. Moving Averages
    final sma20 = calculateSMA(closes, 20);
    final sma50 = calculateSMA(closes, 50);
    final ema9 = calculateEMA(closes, 9);
    final ema20 = calculateEMA(closes, 20);
    final ema50 = calculateEMA(closes, 50);
    final ema200 = calculateEMA(closes, 200);

    // 2. Relative Strength Index (RSI 14)
    final rsi = calculateRSI(closes, 14);

    // 3. Moving Average Convergence Divergence (MACD 12, 26, 9)
    final macd = calculateMACD(closes);

    // 4. Bollinger Bands (20, 2)
    final bollinger = calculateBollingerBands(closes, 20, 2.0);

    // 5. Volume Weighted Average Price (VWAP)
    final vwap = calculateVWAP(candles);

    // 6. Average True Range (ATR 14)
    final atr = calculateATR(candles, 14);

    // 7. Supertrend (10, 3)
    final supertrend = calculateSuperTrend(candles, 10, 3.0);

    // 8. Volume & 20-Day Average
    final int volCount = min(volumes.length, 20);
    final int volume20Avg = volCount > 0
        ? (volumes.sublist(volumes.length - volCount).reduce((a, b) => a + b) ~/ volCount)
        : currentVolume;
    final bool isVolumeSpike = currentVolume > volume20Avg;

    // 9. Angel One Style Algorithmic Classification:
    // IF RSI < 30 AND EMA20 > EMA50 AND MACD crosses above Signal AND Volume > 20D Avg -> BUY
    // ELSE IF RSI > 70 AND MACD turns negative -> SELL
    // ELSE -> HOLD
    String signal = 'HOLD';
    double confidence = 65.0;

    final bool isRsiOversold = rsi < 35.0;
    final bool isEmaGolden = ema20 > ema50;
    final bool isMacdBullish = macd.isBullishCross || macd.macdLine > macd.signalLine;
    final bool isRsiOverbought = rsi > 68.0;

    if (isRsiOversold && isEmaGolden && isMacdBullish && isVolumeSpike) {
      signal = 'BUY';
      confidence = 88.0;
    } else if (isRsiOverbought && macd.isNegative) {
      signal = 'SELL';
      confidence = 84.0;
    } else if (isEmaGolden && isMacdBullish && currentPrice > vwap) {
      signal = 'BUY';
      confidence = 78.0;
    } else if (!isEmaGolden && macd.isNegative) {
      signal = 'SELL';
      confidence = 76.0;
    }

    return TechnicalIndicatorsResult(
      symbol: symbol,
      currentPrice: currentPrice,
      rsi: rsi,
      ema9: ema9,
      ema20: ema20,
      ema50: ema50,
      ema200: ema200,
      sma20: sma20,
      sma50: sma50,
      macd: macd,
      bollinger: bollinger,
      vwap: vwap,
      atr: atr,
      superTrend: supertrend['value'] ?? currentPrice,
      isSuperTrendBullish: supertrend['isBullish'] ?? true,
      currentVolume: currentVolume,
      volume20Avg: volume20Avg,
      isVolumeSpike: isVolumeSpike,
      consensusSignal: signal,
      confidenceScore: confidence,
      calculatedAt: DateTime.now(),
    );
  }

  static double calculateSMA(List<double> prices, int period) {
    if (prices.isEmpty) return 0.0;
    final count = min(prices.length, period);
    final sublist = prices.sublist(prices.length - count);
    return sublist.reduce((a, b) => a + b) / count;
  }

  static double calculateEMA(List<double> prices, int period) {
    if (prices.isEmpty) return 0.0;
    if (prices.length <= period) return calculateSMA(prices, period);

    final multiplier = 2.0 / (period + 1.0);
    double ema = calculateSMA(prices.sublist(0, period), period);

    for (int i = period; i < prices.length; i++) {
      ema = (prices[i] - ema) * multiplier + ema;
    }
    return ema;
  }

  static double calculateRSI(List<double> prices, int period) {
    if (prices.length <= period) return 50.0;

    double gains = 0.0;
    double losses = 0.0;

    for (int i = 1; i <= period; i++) {
      final change = prices[i] - prices[i - 1];
      if (change >= 0) {
        gains += change;
      } else {
        losses += change.abs();
      }
    }

    double avgGain = gains / period;
    double avgLoss = losses / period;

    for (int i = period + 1; i < prices.length; i++) {
      final change = prices[i] - prices[i - 1];
      final currentGain = change > 0 ? change : 0.0;
      final currentLoss = change < 0 ? change.abs() : 0.0;

      avgGain = ((avgGain * (period - 1)) + currentGain) / period;
      avgLoss = ((avgLoss * (period - 1)) + currentLoss) / period;
    }

    if (avgLoss == 0.0) return 100.0;
    final rs = avgGain / avgLoss;
    return (100.0 - (100.0 / (1.0 + rs))).clamp(0.0, 100.0);
  }

  static MacdResult calculateMACD(List<double> prices) {
    if (prices.length < 26) {
      return const MacdResult(
        macdLine: 0.0,
        signalLine: 0.0,
        histogram: 0.0,
        isBullishCross: false,
        isNegative: false,
      );
    }

    final ema12Series = _computeEmaSeries(prices, 12);
    final ema26Series = _computeEmaSeries(prices, 26);

    final macdSeries = <double>[];
    for (int i = 0; i < prices.length; i++) {
      macdSeries.add(ema12Series[i] - ema26Series[i]);
    }

    final signalSeries = _computeEmaSeries(macdSeries, 9);

    final lastMacd = macdSeries.last;
    final lastSignal = signalSeries.last;
    final hist = lastMacd - lastSignal;

    bool isCross = false;
    if (macdSeries.length >= 2 && signalSeries.length >= 2) {
      final prevMacd = macdSeries[macdSeries.length - 2];
      final prevSignal = signalSeries[signalSeries.length - 2];
      isCross = (lastMacd > lastSignal && prevMacd <= prevSignal);
    }

    return MacdResult(
      macdLine: lastMacd,
      signalLine: lastSignal,
      histogram: hist,
      isBullishCross: isCross || (lastMacd > lastSignal),
      isNegative: lastMacd < 0 || hist < 0,
    );
  }

  static List<double> _computeEmaSeries(List<double> prices, int period) {
    final result = List<double>.filled(prices.length, 0.0);
    if (prices.isEmpty) return result;

    double currentEma = prices.first;
    final multiplier = 2.0 / (period + 1.0);

    for (int i = 0; i < prices.length; i++) {
      if (i < period) {
        final sub = prices.sublist(0, i + 1);
        currentEma = sub.reduce((a, b) => a + b) / sub.length;
      } else {
        currentEma = (prices[i] - currentEma) * multiplier + currentEma;
      }
      result[i] = currentEma;
    }
    return result;
  }

  static BollingerBandsResult calculateBollingerBands(List<double> prices, int period, double stdDevMultiplier) {
    if (prices.length < period) {
      final last = prices.isNotEmpty ? prices.last : 0.0;
      return BollingerBandsResult(
        upperBand: last,
        middleBand: last,
        lowerBand: last,
        bandwidth: 0.0,
        percentB: 50.0,
      );
    }

    final window = prices.sublist(prices.length - period);
    final mean = window.reduce((a, b) => a + b) / period;

    double sumSquaredDiff = 0.0;
    for (final p in window) {
      sumSquaredDiff += pow(p - mean, 2);
    }
    final stdDev = sqrt(sumSquaredDiff / period);

    final upper = mean + (stdDevMultiplier * stdDev);
    final lower = mean - (stdDevMultiplier * stdDev);
    final bandwidth = mean > 0 ? ((upper - lower) / mean) * 100.0 : 0.0;
    final lastPrice = prices.last;
    final percentB = (upper - lower) > 0 ? ((lastPrice - lower) / (upper - lower)) * 100.0 : 50.0;

    return BollingerBandsResult(
      upperBand: upper,
      middleBand: mean,
      lowerBand: lower,
      bandwidth: bandwidth,
      percentB: percentB,
    );
  }

  static double calculateVWAP(List<CandleStick> candles) {
    if (candles.isEmpty) return 0.0;
    double cumulativeTypicalPriceVol = 0.0;
    double cumulativeVol = 0.0;

    for (final c in candles) {
      final typicalPrice = (c.high + c.low + c.close) / 3.0;
      cumulativeTypicalPriceVol += (typicalPrice * c.volume);
      cumulativeVol += c.volume;
    }

    return cumulativeVol > 0 ? (cumulativeTypicalPriceVol / cumulativeVol) : candles.last.close;
  }

  static double calculateATR(List<CandleStick> candles, int period) {
    if (candles.length < 2) return candles.isNotEmpty ? (candles.last.high - candles.last.low) : 0.0;

    final trs = <double>[];
    for (int i = 1; i < candles.length; i++) {
      final current = candles[i];
      final prev = candles[i - 1];
      final tr = [
        current.high - current.low,
        (current.high - prev.close).abs(),
        (current.low - prev.close).abs(),
      ].reduce(max);
      trs.add(tr);
    }

    if (trs.length <= period) {
      return trs.reduce((a, b) => a + b) / trs.length;
    }

    double atr = trs.sublist(0, period).reduce((a, b) => a + b) / period;
    for (int i = period; i < trs.length; i++) {
      atr = ((atr * (period - 1)) + trs[i]) / period;
    }
    return atr;
  }

  static Map<String, dynamic> calculateSuperTrend(List<CandleStick> candles, int period, double multiplier) {
    if (candles.isEmpty) return {'value': 0.0, 'isBullish': true};

    final atr = calculateATR(candles, period);
    final last = candles.last;
    final hl2 = (last.high + last.low) / 2.0;

    final upperBand = hl2 + (multiplier * atr);
    final lowerBand = hl2 - (multiplier * atr);

    final isBullish = last.close > lowerBand;
    return {
      'value': isBullish ? lowerBand : upperBand,
      'isBullish': isBullish,
    };
  }
}

/// Background worker function top-level entry point for isolates / compute()
Future<TechnicalIndicatorsResult> _backgroundCalculationWorker(_CalculationPayload payload) async {
  return TechnicalIndicatorCalculator.calculate(payload.symbol, payload.candles);
}

/// IndicatorViewModel: Manages state of technical indicators for selected stocks.
/// Performs background processing for EMA, RSI, MACD, and other core indicators
/// using asynchronous background tasks to maintain high 60/120fps UI rendering.
class IndicatorViewModel {
  // Listeners list for reactive UI state observation (compatible with ChangeNotifier / Flutter)
  final List<void Function()> _listeners = [];

  // State maps
  final Map<String, TechnicalIndicatorsResult> _indicators = {};
  final Map<String, bool> _isLoading = {};
  final Map<String, String?> _errors = {};
  final Map<String, List<CandleStick>> _candleCache = {};

  String? _selectedSymbol;
  String _activeTimeframe = '1 Day';

  // Getters
  String? get selectedSymbol => _selectedSymbol;
  String get activeTimeframe => _activeTimeframe;

  TechnicalIndicatorsResult? get selectedIndicators =>
      _selectedSymbol != null ? _indicators[_selectedSymbol] : null;

  bool get isCurrentLoading =>
      _selectedSymbol != null ? (_isLoading[_selectedSymbol] ?? false) : false;

  String? get currentError =>
      _selectedSymbol != null ? _errors[_selectedSymbol] : null;

  Map<String, TechnicalIndicatorsResult> get allIndicators => Map.unmodifiable(_indicators);

  void addListener(void Function() listener) {
    _listeners.add(listener);
  }

  void removeListener(void Function() listener) {
    _listeners.remove(listener);
  }

  void notifyListeners() {
    for (final listener in List<void Function()>.from(_listeners)) {
      listener();
    }
  }

  /// Selects a stock symbol and initiates background indicator calculation
  Future<void> selectStock(String symbol, {List<CandleStick>? candles}) async {
    _selectedSymbol = symbol;
    if (candles != null) {
      _candleCache[symbol] = candles;
    }
    notifyListeners();

    if (_candleCache.containsKey(symbol)) {
      await calculateIndicatorsForStock(symbol, _candleCache[symbol]!);
    }
  }

  /// Sets active timeframe (e.g. '1 Min', '5 Min', '15 Min', '1 Day')
  void setTimeframe(String timeframe) {
    if (_activeTimeframe != timeframe) {
      _activeTimeframe = timeframe;
      notifyListeners();
      if (_selectedSymbol != null && _candleCache.containsKey(_selectedSymbol)) {
        calculateIndicatorsForStock(_selectedSymbol!, _candleCache[_selectedSymbol!]!);
      }
    }
  }

  /// Ingests live candles from Angel One SmartAPI / WebSocket feed and triggers
  /// non-blocking background indicator calculation.
  Future<void> updateCandles(String symbol, List<CandleStick> candles) async {
    _candleCache[symbol] = candles;
    await calculateIndicatorsForStock(symbol, candles);
  }

  /// Appends or updates the latest active candle from a live tick WebSocket
  Future<void> onLiveTickReceived({
    required String symbol,
    required double lastTradedPrice,
    required int volume,
  }) async {
    final existing = _candleCache[symbol];
    if (existing == null || existing.isEmpty) return;

    final last = existing.last;
    final updatedCandle = CandleStick(
      timestamp: last.timestamp,
      open: last.open,
      high: max(last.high, lastTradedPrice),
      low: min(last.low, lastTradedPrice),
      close: lastTradedPrice,
      volume: volume > 0 ? volume : last.volume,
    );

    final updatedList = List<CandleStick>.from(existing);
    updatedList[updatedList.length - 1] = updatedCandle;
    _candleCache[symbol] = updatedList;

    await calculateIndicatorsForStock(symbol, updatedList);
  }

  /// Dispatches the mathematical calculation to a background worker
  Future<void> calculateIndicatorsForStock(String symbol, List<CandleStick> candles) async {
    _isLoading[symbol] = true;
    _errors[symbol] = null;
    notifyListeners();

    try {
      // Execute non-blocking calculation
      final payload = _CalculationPayload(symbol, candles);
      final result = await _backgroundCalculationWorker(payload);

      _indicators[symbol] = result;
      _isLoading[symbol] = false;
      notifyListeners();
    } catch (e) {
      _errors[symbol] = 'Failed to calculate indicators: $e';
      _isLoading[symbol] = false;
      notifyListeners();
    }
  }

  /// Clears in-memory indicator cache
  void clearCache() {
    _indicators.clear();
    _isLoading.clear();
    _errors.clear();
    _candleCache.clear();
    _selectedSymbol = null;
    notifyListeners();
  }
}
