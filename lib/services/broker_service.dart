// lib/services/broker_service.dart
import 'dart:async';
import 'dart:convert';
import 'dart:io';
import '../models/market_status.dart';
import 'token_storage_service.dart';

class BrokerService {
  final String baseUrl;
  final HttpClient _httpClient;
  final TokenStorageService _tokenStorage;

  final _brokerStatusController = StreamController<BrokerStatusModel>.broadcast();
  Stream<BrokerStatusModel> get brokerStatusStream => _brokerStatusController.stream;

  BrokerStatusModel _currentStatus = BrokerStatusModel.notConnected('');
  BrokerStatusModel get currentStatus => _currentStatus;

  BrokerService({
    String? baseUrl,
    HttpClient? httpClient,
    TokenStorageService? tokenStorage,
  })  : baseUrl = baseUrl ?? 'https://ais-dev-fqjcymvmbutrgbvk2sc62f-536147365589.asia-east1.run.app',
        _httpClient = httpClient ?? HttpClient(),
        _tokenStorage = tokenStorage ?? TokenStorageService();

  Future<Map<String, dynamic>> _post(String path, Map<String, dynamic> body) async {
    final uri = Uri.parse('$baseUrl$path');
    final request = await _httpClient.postUrl(uri).timeout(const Duration(seconds: 10));
    final token = _tokenStorage.accessToken;
    if (token != null && token.isNotEmpty) {
      request.headers.set(HttpHeaders.authorizationHeader, 'Bearer $token');
    }
    request.headers.set(HttpHeaders.contentTypeHeader, 'application/json; charset=UTF-8');
    request.headers.set(HttpHeaders.acceptHeader, 'application/json');
    request.write(jsonEncode(body));

    final response = await request.close().timeout(const Duration(seconds: 10));
    final responseBody = await response.transform(utf8.decoder).join();
    final decoded = jsonDecode(responseBody) as Map<String, dynamic>;

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decoded;
    } else {
      final msg = decoded['detail'] ?? decoded['message'] ?? 'Broker error (${response.statusCode})';
      throw HttpException(msg.toString());
    }
  }

  Future<Map<String, dynamic>> _get(String path) async {
    final uri = Uri.parse('$baseUrl$path');
    final request = await _httpClient.getUrl(uri).timeout(const Duration(seconds: 8));
    final token = _tokenStorage.accessToken;
    if (token != null && token.isNotEmpty) {
      request.headers.set(HttpHeaders.authorizationHeader, 'Bearer $token');
    }
    request.headers.set(HttpHeaders.acceptHeader, 'application/json');

    final response = await request.close().timeout(const Duration(seconds: 8));
    final responseBody = await response.transform(utf8.decoder).join();
    if (response.statusCode >= 200 && response.statusCode < 300) {
      return jsonDecode(responseBody) as Map<String, dynamic>;
    } else {
      throw HttpException('Failed to fetch broker status (${response.statusCode})');
    }
  }

  /// GET /api/v1/broker/status
  Future<BrokerStatusModel> fetchStatus() async {
    try {
      final json = await _get('/api/v1/broker/status');
      _currentStatus = BrokerStatusModel.fromJson(json);
      _brokerStatusController.add(_currentStatus);
      return _currentStatus;
    } catch (_) {
      final fallback = BrokerStatusModel.notConnected('ANGEL_ONE');
      _currentStatus = fallback;
      _brokerStatusController.add(fallback);
      return fallback;
    }
  }

  /// POST /api/v1/broker/connect
  Future<BrokerStatusModel> connectAngelOne({
    required String clientCode,
    String? apiKey,
    String? mpin,
    String? totp,
  }) async {
    _currentStatus = BrokerStatusModel(
      brokerName: 'ANGEL_ONE',
      clientCode: clientCode,
      state: BrokerConnectionState.connecting,
    );
    _brokerStatusController.add(_currentStatus);

    try {
      final payload = {
        'broker_name': 'ANGEL_ONE',
        'client_code': clientCode,
        'api_key': apiKey ?? 's6BvE892YqW91',
        'mpin_or_password': mpin ?? '1234',
        'totp': totp ?? '729410',
      };
      final json = await _post('/api/v1/broker/connect', payload);
      _currentStatus = BrokerStatusModel.fromJson(json);
      _brokerStatusController.add(_currentStatus);
      return _currentStatus;
    } catch (e) {
      _currentStatus = BrokerStatusModel(
        brokerName: 'ANGEL_ONE',
        clientCode: clientCode,
        state: BrokerConnectionState.error,
        lastErrorMessage: e.toString().replaceAll('Exception: ', ''),
      );
      _brokerStatusController.add(_currentStatus);
      rethrow;
    }
  }

  /// POST /api/v1/broker/disconnect
  Future<BrokerStatusModel> disconnect() async {
    try {
      final json = await _post('/api/v1/broker/disconnect', {});
      _currentStatus = BrokerStatusModel.fromJson(json);
      _brokerStatusController.add(_currentStatus);
      return _currentStatus;
    } catch (_) {
      _currentStatus = BrokerStatusModel.notConnected('');
      _brokerStatusController.add(_currentStatus);
      return _currentStatus;
    }
  }

  void dispose() {
    _brokerStatusController.close();
  }
}
