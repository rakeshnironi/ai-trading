// lib/models/market_status.dart

enum MarketSessionState {
  preMarket,
  marketOpen,
  marketClosed,
  holiday,
  dataDelayed,
  connectionError,
}

enum BrokerConnectionState {
  notConnected,
  connecting,
  connected,
  expired,
  error,
}

class MarketStatusModel {
  final String exchange;
  final String timezone;
  final String rawStatus;
  final bool isMarketOpen;
  final String sessionType;
  final String? sessionName;
  final DateTime currentTime;
  final DateTime? nextOpen;
  final DateTime? nextClose;
  final bool isHoliday;
  final String? holidayName;
  final String dataStatus;
  final DateTime? lastMarketDataTimestamp;

  const MarketStatusModel({
    required this.exchange,
    required this.timezone,
    required this.rawStatus,
    required this.isMarketOpen,
    required this.sessionType,
    this.sessionName,
    required this.currentTime,
    this.nextOpen,
    this.nextClose,
    required this.isHoliday,
    this.holidayName,
    required this.dataStatus,
    this.lastMarketDataTimestamp,
  });

  MarketSessionState get sessionState {
    if (isHoliday) return MarketSessionState.holiday;
    if (isMarketOpen) return MarketSessionState.marketOpen;
    if (rawStatus == 'PRE_MARKET') return MarketSessionState.preMarket;
    return MarketSessionState.marketClosed;
  }

  String get displayLabel {
    switch (sessionState) {
      case MarketSessionState.marketOpen:
        return 'MARKET OPEN';
      case MarketSessionState.preMarket:
        return 'PRE-MARKET';
      case MarketSessionState.holiday:
        return 'HOLIDAY: ${holidayName ?? "EXCHANGE CLOSED"}';
      case MarketSessionState.marketClosed:
        return 'MARKET CLOSED';
      case MarketSessionState.dataDelayed:
        return 'DATA DELAYED';
      case MarketSessionState.connectionError:
        return 'CONNECTION ERROR';
    }
  }

  factory MarketStatusModel.fromJson(Map<String, dynamic> json) {
    return MarketStatusModel(
      exchange: json['exchange'] as String? ?? 'NSE',
      timezone: json['timezone'] as String? ?? 'Asia/Kolkata',
      rawStatus: json['market_status'] as String? ?? 'MARKET_CLOSED',
      isMarketOpen: json['is_market_open'] as bool? ?? false,
      sessionType: json['session_type'] as String? ?? 'REGULAR',
      sessionName: json['session_name'] as String?,
      currentTime: DateTime.tryParse(json['current_time'] as String? ?? '') ?? DateTime.now(),
      nextOpen: json['next_open'] != null ? DateTime.tryParse(json['next_open'] as String) : null,
      nextClose: json['next_close'] != null ? DateTime.tryParse(json['next_close'] as String) : null,
      isHoliday: json['is_holiday'] as bool? ?? false,
      holidayName: json['holiday_name'] as String?,
      dataStatus: json['data_status'] as String? ?? 'LAST_AVAILABLE',
      lastMarketDataTimestamp: json['last_market_data_timestamp'] != null
          ? DateTime.tryParse(json['last_market_data_timestamp'] as String)
          : null,
    );
  }

  static MarketStatusModel fallbackClosed() {
    final now = DateTime.now();
    return MarketStatusModel(
      exchange: 'NSE',
      timezone: 'Asia/Kolkata',
      rawStatus: 'MARKET_CLOSED',
      isMarketOpen: false,
      sessionType: 'REGULAR',
      sessionName: 'Session Closed (Calculated from IST Calendar)',
      currentTime: now,
      isHoliday: now.weekday >= 6,
      holidayName: now.weekday >= 6 ? 'Weekend Holiday' : null,
      dataStatus: 'LAST_AVAILABLE',
      lastMarketDataTimestamp: null,
    );
  }
}

class BrokerStatusModel {
  final String brokerName;
  final String clientCode;
  final BrokerConnectionState state;
  final DateTime? connectedAt;
  final DateTime? expiresAt;
  final String? lastErrorMessage;
  final int holdingsCount;
  final int positionsCount;
  final double availableMargin;

  const BrokerStatusModel({
    required this.brokerName,
    required this.clientCode,
    required this.state,
    this.connectedAt,
    this.expiresAt,
    this.lastErrorMessage,
    this.holdingsCount = 0,
    this.positionsCount = 0,
    this.availableMargin = 0.0,
  });

  bool get isConnected => state == BrokerConnectionState.connected;

  factory BrokerStatusModel.fromJson(Map<String, dynamic> json) {
    final rawStatus = (json['connection_status'] as String? ?? 'NOT_CONNECTED').toUpperCase();
    BrokerConnectionState connState;
    switch (rawStatus) {
      case 'CONNECTED':
        connState = BrokerConnectionState.connected;
        break;
      case 'CONNECTING':
        connState = BrokerConnectionState.connecting;
        break;
      case 'EXPIRED':
        connState = BrokerConnectionState.expired;
        break;
      case 'ERROR':
        connState = BrokerConnectionState.error;
        break;
      default:
        connState = BrokerConnectionState.notConnected;
    }

    return BrokerStatusModel(
      brokerName: json['broker_name'] as String? ?? 'ANGEL_ONE',
      clientCode: json['client_code'] as String? ?? '',
      state: connState,
      connectedAt: json['connected_at'] != null ? DateTime.tryParse(json['connected_at'] as String) : null,
      expiresAt: json['expires_at'] != null ? DateTime.tryParse(json['expires_at'] as String) : null,
      lastErrorMessage: json['last_error_message'] as String?,
      holdingsCount: json['holdings_count'] as int? ?? 0,
      positionsCount: json['positions_count'] as int? ?? 0,
      availableMargin: (json['available_margin'] as num?)?.toDouble() ?? 0.0,
    );
  }

  static BrokerStatusModel notConnected(String clientCode) {
    return BrokerStatusModel(
      brokerName: 'ANGEL_ONE',
      clientCode: clientCode,
      state: BrokerConnectionState.notConnected,
    );
  }
}

class LiveStockQuote {
  final String symbol;
  final String exchange;
  final double ltp;
  final double open;
  final double high;
  final double low;
  final double close;
  final double volume;
  final double change;
  final double changePct;
  final DateTime timestamp;
  final String dataStatus; // REAL_TIME or LAST_AVAILABLE

  const LiveStockQuote({
    required this.symbol,
    required this.exchange,
    required this.ltp,
    required this.open,
    required this.high,
    required this.low,
    required this.close,
    required this.volume,
    required this.change,
    required this.changePct,
    required this.timestamp,
    this.dataStatus = 'REAL_TIME',
  });

  factory LiveStockQuote.fromJson(Map<String, dynamic> json, {String dataStatus = 'REAL_TIME'}) {
    return LiveStockQuote(
      symbol: json['symbol'] as String? ?? '',
      exchange: json['exchange'] as String? ?? 'NSE',
      ltp: (json['ltp'] as num?)?.toDouble() ?? 0.0,
      open: (json['open'] as num?)?.toDouble() ?? 0.0,
      high: (json['high'] as num?)?.toDouble() ?? 0.0,
      low: (json['low'] as num?)?.toDouble() ?? 0.0,
      close: (json['close'] as num?)?.toDouble() ?? 0.0,
      volume: (json['volume'] as num?)?.toDouble() ?? 0.0,
      change: (json['change'] as num?)?.toDouble() ?? 0.0,
      changePct: (json['change_pct'] as num?)?.toDouble() ?? 0.0,
      timestamp: DateTime.tryParse(json['timestamp'] as String? ?? '') ?? DateTime.now(),
      dataStatus: dataStatus,
    );
  }
}
