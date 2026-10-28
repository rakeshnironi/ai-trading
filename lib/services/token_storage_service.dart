// lib/services/token_storage_service.dart
import 'dart:async';

/// Secure Token Storage Service
/// Enforces Security Rules:
/// - Never store raw OTP locally; only persist verified JWT tokens.
/// - Clear all tokens, session states, and broker credentials immediately on logout.
/// - Notify all dependent services to cancel polling and release WebSocket connections.
class TokenStorageService {
  static final TokenStorageService _instance = TokenStorageService._internal();
  factory TokenStorageService() => _instance;
  TokenStorageService._internal();

  String? _accessToken;
  String? _refreshToken;
  int? _userId;
  String? _userEmail;

  final _logoutStreamController = StreamController<void>.broadcast();
  Stream<void> get onLogout => _logoutStreamController.stream;

  /// Store securely received JWT tokens
  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
    required int userId,
    String? email,
  }) async {
    _accessToken = accessToken;
    _refreshToken = refreshToken;
    _userId = userId;
    _userEmail = email;
  }

  String? get accessToken => _accessToken;
  String? get refreshToken => _refreshToken;
  int? get userId => _userId;
  String? get userEmail => _userEmail;

  bool get isAuthenticated => _accessToken != null && _accessToken!.isNotEmpty;

  /// Complete Session Clear on Logout (Part 5)
  Future<void> clear() async {
    _accessToken = null;
    _refreshToken = null;
    _userId = null;
    _userEmail = null;
    _logoutStreamController.add(null);
  }
}
