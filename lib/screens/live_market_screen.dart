// lib/screens/live_market_screen.dart
import 'package:flutter/material.dart';
import '../models/market_status.dart';
import '../services/market_api_service.dart';
import '../services/broker_service.dart';
import '../services/token_storage_service.dart';

class LiveMarketScreen extends StatefulWidget {
  const LiveMarketScreen({Key? key}) : super(key: key);

  @override
  State<LiveMarketScreen> createState() => _LiveMarketScreenState();
}

class _LiveMarketScreenState extends State<LiveMarketScreen> {
  final MarketApiService _marketApi = MarketApiService();
  final BrokerService _brokerService = BrokerService();
  final TokenStorageService _tokenStorage = TokenStorageService();

  MarketStatusModel _marketStatus = MarketStatusModel.fallbackClosed();
  List<LiveStockQuote> _quotes = [];
  BrokerStatusModel _brokerStatus = BrokerStatusModel.notConnected('ANGEL_ONE');
  bool _isPaperMode = true; // Default safe mode
  bool _isLoading = true;
  bool _isStaleData = false;

  // Design Palette (Groww & Angel One Inspired)
  static const Color primaryGreen = Color(0xFF00D09C);
  static const Color deepNavy = Color(0xFF0F172A);
  static const Color cardBg = Color(0xFFFFFFFF);
  static const Color screenBg = Color(0xFFF8FAFC);
  static const Color textMuted = Color(0xFF64748B);
  static const Color borderGrey = Color(0xFFE2E8F0);
  static const Color alertRed = Color(0xFFEF4444);
  static const Color warningAmber = Color(0xFFF59E0B);
  static const Color angelOrange = Color(0xFFFF5722);

  @override
  void initState() {
    super.initState();
    _initData();

    // Listen to market status updates
    _marketApi.marketStatusStream.listen((status) {
      if (mounted) setState(() => _marketStatus = status);
    });

    // Listen to quotes
    _marketApi.quotesStream.listen((quotesList) {
      if (mounted) {
        setState(() {
          _quotes = quotesList;
          _isLoading = false;
        });
      }
    });

    // Listen to stale data warnings
    _marketApi.staleDataWarningStream.listen((isStale) {
      if (mounted) setState(() => _isStaleData = isStale);
    });

    // Listen to broker status
    _brokerService.brokerStatusStream.listen((bStatus) {
      if (mounted) setState(() => _brokerStatus = bStatus);
    });

    // Listen to session logout
    _tokenStorage.onLogout.listen((_) {
      _marketApi.toggleSmartKey(false);
      _marketApi.dispose();
      _brokerService.dispose();
    });
  }

  Future<void> _initData() async {
    setState(() => _isLoading = true);
    await Future.wait([
      _marketApi.fetchMarketStatus().then((s) => _marketStatus = s),
      _marketApi.fetchQuotes().then((q) => _quotes = q),
      _brokerService.fetchStatus().then((b) => _brokerStatus = b),
    ]);
    if (mounted) setState(() => _isLoading = false);
  }

  @override
  void dispose() {
    _marketApi.dispose();
    _brokerService.dispose();
    super.dispose();
  }

  // --- LOGOUT CONFIRMATION DIALOG (PART 5) ---
  void _confirmLogout() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Row(
          children: const [
            Icon(Icons.logout_rounded, color: alertRed, size: 24),
            SizedBox(width: 8),
            Text('Confirm Logout', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
          ],
        ),
        content: const Text(
          'Are you sure you want to log out? All active sessions, tokens, and background streaming will be disconnected.',
          style: TextStyle(color: textMuted, fontSize: 14),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancel', style: TextStyle(color: textMuted)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: alertRed,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            ),
            onPressed: () async {
              Navigator.of(ctx).pop();
              await _performCompleteLogout();
            },
            child: const Text('Log Out', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  Future<void> _performCompleteLogout() async {
    // 1. Terminate Smart Key polling
    _marketApi.toggleSmartKey(false);

    // 2. Clear token storage
    await _tokenStorage.clear();

    // 3. Navigate to login and pop backstack completely
    if (mounted) {
      Navigator.of(context).pushNamedAndRemoveUntil('/login', (route) => false);
    }
  }

  // --- CONNECT BROKER MODAL (PART 4) ---
  void _showBrokerConnectDialog() {
    final clientCodeCtrl = TextEditingController(text: 'R982734');
    final mpinCtrl = TextEditingController(text: '1234');
    final totpCtrl = TextEditingController(text: '729410');
    final apiKeyCtrl = TextEditingController();
    bool showAdvancedApiKey = false;
    bool isSubmitting = false;
    String? errorText;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModalState) => Padding(
          padding: EdgeInsets.only(
            left: 24,
            right: 24,
            top: 24,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: angelOrange.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: const Icon(Icons.account_balance, color: angelOrange, size: 24),
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Connect Angel One Demat',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: deepNavy),
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.of(ctx).pop(),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                const Text(
                  'Authenticate directly with official Angel One SmartAPI V2 (loginByPassword).',
                  style: TextStyle(color: textMuted, fontSize: 13),
                ),
                if (errorText != null) ...[
                  const SizedBox(height: 12),
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: alertRed.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: alertRed.withOpacity(0.3)),
                    ),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Icon(Icons.error_outline, color: alertRed, size: 18),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(errorText!, style: const TextStyle(color: alertRed, fontSize: 12, height: 1.4)),
                        ),
                      ],
                    ),
                  ),
                ],
                const SizedBox(height: 16),
                TextField(
                  controller: clientCodeCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Client Code / UCC',
                    hintText: 'e.g. R982734 (NOT your mobile number)',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.badge_outlined),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: mpinCtrl,
                  obscureText: true,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: '4-Digit Trading MPIN',
                    hintText: 'e.g. 1234',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.lock_outline),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: totpCtrl,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: '6-Digit TOTP from Authenticator App',
                    hintText: 'e.g. 729410',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.security),
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  '📌 Tip: Open Google/Microsoft Authenticator and ensure your device clock is synchronized to standard internet time before submitting.',
                  style: TextStyle(fontSize: 11, color: textMuted, height: 1.3),
                ),
                const SizedBox(height: 10),
                GestureDetector(
                  onTap: () => setModalState(() => showAdvancedApiKey = !showAdvancedApiKey),
                  child: Row(
                    children: [
                      Icon(showAdvancedApiKey ? Icons.arrow_drop_up : Icons.arrow_drop_down, color: textMuted),
                      Text(
                        showAdvancedApiKey ? 'Hide Custom SmartAPI Key' : 'Advanced: Custom SmartAPI Key (Optional)',
                        style: const TextStyle(fontSize: 12, color: textMuted, fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                ),
                if (showAdvancedApiKey) ...[
                  const SizedBox(height: 8),
                  TextField(
                    controller: apiKeyCtrl,
                    decoration: const InputDecoration(
                      labelText: 'SmartAPI Key (Leave empty to use server default)',
                      hintText: 'e.g. s6BvE892YqW91',
                      border: OutlineInputBorder(),
                      prefixIcon: Icon(Icons.vpn_key_outlined),
                    ),
                  ),
                ],
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: angelOrange,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                    onPressed: isSubmitting
                        ? null
                        : () async {
                            final cCode = clientCodeCtrl.text.trim();
                            final mpin = mpinCtrl.text.trim();
                            final totp = totpCtrl.text.trim();

                            if (cCode.isEmpty) {
                              setModalState(() => errorText = 'Please enter your Angel One Client Code.');
                              return;
                            }
                            if (cCode.length == 10 && int.tryParse(cCode) != null) {
                              setModalState(() => errorText = 'Please enter your Angel One Client ID (e.g. R982734), not your 10-digit mobile number.');
                              return;
                            }
                            if (mpin.isEmpty) {
                              setModalState(() => errorText = 'Please enter your 4-digit Trading MPIN.');
                              return;
                            }
                            if (totp.isEmpty) {
                              setModalState(() => errorText = 'Please enter the 6-digit TOTP code from your Authenticator app.');
                              return;
                            }

                            setModalState(() {
                              isSubmitting = true;
                              errorText = null;
                            });
                            try {
                              await _brokerService.connectAngelOne(
                                clientCode: cCode,
                                mpin: mpin,
                                totp: totp,
                                apiKey: apiKeyCtrl.text.trim().isNotEmpty ? apiKeyCtrl.text.trim() : null,
                              );
                              Navigator.of(ctx).pop();
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('✓ Angel One Demat account authenticated successfully!'),
                                  backgroundColor: primaryGreen,
                                ),
                              );
                            } catch (e) {
                              setModalState(() {
                                isSubmitting = false;
                                errorText = e.toString().replaceAll('Exception: ', '').replaceAll('HttpException: ', '');
                              });
                            }
                          },
                    child: isSubmitting
                        ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                        : const Text('Authorize & Link Demat', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
                      ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                      : const Text('Authorize & Link Demat', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: screenBg,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0.5,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Autonomous Trading',
              style: TextStyle(color: deepNavy, fontWeight: FontWeight.w800, fontSize: 17),
            ),
            Text(
              _isPaperMode ? 'Paper Trading Engine (₹10,00,000)' : 'Live Market Engine (Angel One)',
              style: TextStyle(color: _isPaperMode ? primaryGreen : angelOrange, fontSize: 11, fontWeight: FontWeight.w600),
            ),
          ],
        ),
        actions: [
          // Mode Toggle (Part 7)
          Container(
            margin: const EdgeInsets.symmetric(vertical: 10),
            decoration: BoxDecoration(
              color: screenBg,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: borderGrey),
            ),
            child: Row(
              children: [
                _buildModeBtn('Paper', _isPaperMode, () => setState(() => _isPaperMode = true)),
                _buildModeBtn('Live', !_isPaperMode, () {
                  if (!_brokerStatus.isConnected) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Please connect a Demat / Broker account to execute Live trades.'),
                        backgroundColor: angelOrange,
                      ),
                    );
                  }
                  setState(() => _isPaperMode = false);
                }),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.logout_rounded, color: textMuted),
            tooltip: 'Logout',
            onPressed: _confirmLogout,
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _initData,
        color: primaryGreen,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // 1. Authoritative Market Status Card (Part 1 & 3)
            _buildMarketStatusHeader(),

            const SizedBox(height: 12),

            // 2. Stale Data Warning Banner if applicable
            if (_isStaleData) _buildStaleDataBanner(),

            // 3. Smart Key Status & Control Bar (Part 2)
            _buildSmartKeyControlCard(),

            const SizedBox(height: 12),

            // 4. Broker / Demat Account Connection Card (Part 4)
            _buildBrokerConnectionCard(),

            const SizedBox(height: 16),

            // 5. Watchlist / Market Quotes Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Active NSE Universe Quotes',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: deepNavy),
                ),
                Text(
                  _marketStatus.isMarketOpen ? 'LIVE TICKS' : 'LAST CLOSING PRICES',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: _marketStatus.isMarketOpen ? primaryGreen : textMuted,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 8),

            // 6. Stock List Cards
            if (_isLoading)
              const Center(child: Padding(padding: EdgeInsets.all(40), child: CircularProgressIndicator()))
            else
              ..._quotes.map((quote) => _buildStockCard(quote)).toList(),
          ],
        ),
      ),
    );
  }

  Widget _buildModeBtn(String text, bool active, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: active ? deepNavy : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Text(
          text,
          style: TextStyle(
            color: active ? Colors.white : textMuted,
            fontWeight: FontWeight.bold,
            fontSize: 11,
          ),
        ),
      ),
    );
  }

  // --- MARKET STATUS HEADER CARD (PART 1, 3 & 8) ---
  Widget _buildMarketStatusHeader() {
    final state = _marketStatus.sessionState;
    Color statusColor;
    IconData statusIcon;

    switch (state) {
      case MarketSessionState.marketOpen:
        statusColor = primaryGreen;
        statusIcon = Icons.check_circle_rounded;
        break;
      case MarketSessionState.preMarket:
        statusColor = const Color(0xFF0284C7);
        statusIcon = Icons.access_time_filled;
        break;
      case MarketSessionState.holiday:
        statusColor = warningAmber;
        statusIcon = Icons.beach_access_rounded;
        break;
      case MarketSessionState.marketClosed:
      default:
        statusColor = textMuted;
        statusIcon = Icons.lock_clock;
    }

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: statusColor.withOpacity(0.35), width: 1.5),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.03), blurRadius: 8, offset: const Offset(0, 2)),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 10,
                    height: 10,
                    decoration: BoxDecoration(color: statusColor, shape: BoxShape.circle),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    _marketStatus.displayLabel,
                    style: TextStyle(color: statusColor, fontWeight: FontWeight.w900, fontSize: 13, letterSpacing: 0.5),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(color: screenBg, borderRadius: BorderRadius.circular(6)),
                child: const Text('NSE / BSE India (IST)', style: TextStyle(fontSize: 10, color: textMuted, fontWeight: FontWeight.bold)),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            _marketStatus.isMarketOpen
                ? 'Regular Trading Session Active (09:15 - 15:30 IST)'
                : (_marketStatus.isHoliday
                    ? 'Exchange is closed for official holiday: ${_marketStatus.holidayName ?? "NSE Holiday"}'
                    : 'Exchange is closed. Showing official last closing session prices.'),
            style: const TextStyle(fontSize: 12, color: deepNavy, fontWeight: FontWeight.w500),
          ),
          if (_marketStatus.nextOpen != null) ...[
            const SizedBox(height: 6),
            Text(
              'Next Open: ${_formatDateTime(_marketStatus.nextOpen!)} IST',
              style: const TextStyle(fontSize: 11, color: textMuted),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildStaleDataBanner() {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: warningAmber.withOpacity(0.12),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: warningAmber.withOpacity(0.4)),
      ),
      child: Row(
        children: const [
          Icon(Icons.warning_amber_rounded, color: warningAmber, size: 20),
          SizedBox(width: 10),
          Expanded(
            child: Text(
              'Market data feed delayed or reconnecting. Displaying last verified prices.',
              style: TextStyle(color: Color(0xFFB45309), fontSize: 12, fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }

  // --- SMART KEY CONTROL CARD (PART 2) ---
  Widget _buildSmartKeyControlCard() {
    final isSmartKeyOn = _marketApi.isSmartKeyActive;

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: isSmartKeyOn ? primaryGreen.withOpacity(0.5) : borderGrey),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: (isSmartKeyOn ? primaryGreen : textMuted).withOpacity(0.12),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(Icons.vpn_key_rounded, color: isSmartKeyOn ? primaryGreen : textMuted, size: 20),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Smart Key Stream', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: deepNavy)),
                  Text(
                    isSmartKeyOn
                        ? (_marketStatus.isMarketOpen ? '● Active Live Feed' : '⏸ Paused (Off-Market Hours)')
                        : 'Inactive (Toggle to connect)',
                    style: TextStyle(
                      fontSize: 11,
                      color: isSmartKeyOn ? (_marketStatus.isMarketOpen ? primaryGreen : warningAmber) : textMuted,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ],
          ),
          Switch(
            value: isSmartKeyOn,
            activeColor: primaryGreen,
            onChanged: (val) {
              setState(() {
                _marketApi.toggleSmartKey(val);
              });
              if (val && !_marketStatus.isMarketOpen) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Smart Key activated. Market is currently closed; live micro-ticks will stream during active hours (09:15 - 15:30 IST).'),
                    backgroundColor: deepNavy,
                    duration: Duration(seconds: 4),
                  ),
                );
              }
            },
          ),
        ],
      ),
    );
  }

  // --- DEMAT / BROKER CONNECTION CARD (PART 4) ---
  Widget _buildBrokerConnectionCard() {
    final isConnected = _brokerStatus.isConnected;

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: isConnected ? angelOrange.withOpacity(0.5) : borderGrey),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: angelOrange.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.account_balance_wallet, color: angelOrange, size: 20),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    isConnected ? 'Demat: ${_brokerStatus.clientCode}' : 'No Demat Account Connected',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: deepNavy),
                  ),
                  Text(
                    isConnected
                        ? 'Status: CONNECTED (Angel One SmartAPI)'
                        : 'Connect your broker to retrieve personal holdings',
                    style: TextStyle(
                      fontSize: 11,
                      color: isConnected ? primaryGreen : textMuted,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ],
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: isConnected ? screenBg : angelOrange,
              foregroundColor: isConnected ? deepNavy : Colors.white,
              elevation: 0,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: isConnected ? const BorderSide(color: borderGrey) : BorderSide.none,
              ),
            ),
            onPressed: isConnected
                ? () async {
                    await _brokerService.disconnect();
                    setState(() {});
                  }
                : _showBrokerConnectDialog,
            child: Text(
              isConnected ? 'Disconnect' : 'Connect Broker',
              style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStockCard(LiveStockQuote quote) {
    final isPos = quote.change >= 0;
    final changeColor = isPos ? primaryGreen : alertRed;

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: borderGrey),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(quote.symbol, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14, color: deepNavy)),
              const SizedBox(height: 2),
              Row(
                children: [
                  Text(quote.exchange, style: const TextStyle(fontSize: 10, color: textMuted, fontWeight: FontWeight.bold)),
                  const SizedBox(width: 6),
                  if (!_marketStatus.isMarketOpen)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 1),
                      decoration: BoxDecoration(color: screenBg, borderRadius: BorderRadius.circular(4)),
                      child: const Text('LAST CLOSE', style: TextStyle(fontSize: 8, color: textMuted, fontWeight: FontWeight.bold)),
                    ),
                ],
              ),
            ],
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '₹${quote.ltp.toStringAsFixed(2)}',
                style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14, color: deepNavy),
              ),
              const SizedBox(height: 2),
              Text(
                '${isPos ? "+" : ""}${quote.change.toStringAsFixed(2)} (${isPos ? "+" : ""}${quote.changePct.toStringAsFixed(2)}%)',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11, color: changeColor),
              ),
            ],
          ),
        ],
      ),
    );
  }

  String _formatDateTime(DateTime dt) {
    final hour = dt.hour > 12 ? dt.hour - 12 : (dt.hour == 0 ? 12 : dt.hour);
    final ampm = dt.hour >= 12 ? 'PM' : 'AM';
    final minute = dt.minute.toString().padLeft(2, '0');
    return '${dt.day}/${dt.month}/${dt.year} $hour:$minute $ampm';
  }
}
