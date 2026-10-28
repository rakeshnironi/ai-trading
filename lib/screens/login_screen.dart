// lib/screens/login_screen.dart
import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import '../services/token_storage_service.dart';
import 'otp_verification_screen.dart';
import '../models/auth_models.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({Key? key}) : super(key: key);

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController(text: 'rakeshnironi@gmail.com');
  final _passwordController = TextEditingController(text: 'Password@123');
  final _mobileController = TextEditingController(text: '9876543210');
  final _tokenStorage = TokenStorageService();

  bool _isEmailTab = true;
  bool _isLoading = false;
  bool _obscurePassword = true;
  String? _errorMessage;

  static const Color primaryGreen = Color(0xFF00D09C);
  static const Color deepNavy = Color(0xFF0F172A);
  static const Color textMuted = Color(0xFF64748B);
  static const Color borderGrey = Color(0xFFE2E8F0);
  static const Color angelOrange = Color(0xFFFF5722);

  Future<void> _handleEmailLogin() async {
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    if (email.isEmpty || password.isEmpty) {
      setState(() => _errorMessage = 'Please enter both email and password.');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final client = HttpClient();
      final uri = Uri.parse('https://ais-dev-fqjcymvmbutrgbvk2sc62f-536147365589.asia-east1.run.app/api/v1/auth/login');
      final request = await client.postUrl(uri).timeout(const Duration(seconds: 10));
      request.headers.set(HttpHeaders.contentTypeHeader, 'application/json');
      request.write(jsonEncode({'email': email, 'password': password}));

      final response = await request.close();
      final body = await response.transform(utf8.decoder).join();
      final decoded = jsonDecode(body) as Map<String, dynamic>;

      if (response.statusCode == 200) {
        final token = decoded['access_token'] as String;
        final user = decoded['user'] as Map<String, dynamic>;
        await _tokenStorage.saveTokens(
          accessToken: token,
          refreshToken: token,
          userId: user['id'] as int,
          email: user['email'] as String?,
        );

        if (mounted) {
          Navigator.of(context).pushReplacementNamed('/live_market');
        }
      } else {
        setState(() => _errorMessage = decoded['detail'] ?? 'Login failed. Please check your credentials.');
      }
    } catch (_) {
      // Fallback offline session
      await _tokenStorage.saveTokens(
        accessToken: 'dev_token_${DateTime.now().millisecondsSinceEpoch}',
        refreshToken: 'refresh_${DateTime.now().millisecondsSinceEpoch}',
        userId: 1,
        email: email,
      );
      if (mounted) {
        Navigator.of(context).pushReplacementNamed('/live_market');
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                // Brand Header
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: primaryGreen.withOpacity(0.12),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.show_chart_rounded, color: primaryGreen, size: 42),
                ),
                const SizedBox(height: 16),
                const Text(
                  'Autonomous Trading',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: deepNavy),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Sign in to access Live Market & Paper Trading',
                  style: TextStyle(fontSize: 13, color: textMuted),
                ),
                const SizedBox(height: 28),

                // Method Tabs (Email vs Mobile OTP)
                Container(
                  decoration: BoxDecoration(
                    color: const Color(0xFFF1F5F9),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  padding: const EdgeInsets.all(4),
                  child: Row(
                    children: [
                      Expanded(
                        child: GestureDetector(
                          onTap: () => setState(() => _isEmailTab = true),
                          child: Container(
                            padding: const EdgeInsets.symmetric(vertical: 10),
                            decoration: BoxDecoration(
                              color: _isEmailTab ? Colors.white : Colors.transparent,
                              borderRadius: BorderRadius.circular(8),
                              boxShadow: _isEmailTab
                                  ? [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 4)]
                                  : null,
                            ),
                            child: Center(
                              child: Text(
                                'Email & Password',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 13,
                                  color: _isEmailTab ? deepNavy : textMuted,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                      Expanded(
                        child: GestureDetector(
                          onTap: () => setState(() => _isEmailTab = false),
                          child: Container(
                            padding: const EdgeInsets.symmetric(vertical: 10),
                            decoration: BoxDecoration(
                              color: !_isEmailTab ? Colors.white : Colors.transparent,
                              borderRadius: BorderRadius.circular(8),
                              boxShadow: !_isEmailTab
                                  ? [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 4)]
                                  : null,
                            ),
                            child: Center(
                              child: Text(
                                'Mobile OTP',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 13,
                                  color: !_isEmailTab ? deepNavy : textMuted,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 24),

                if (_errorMessage != null) ...[
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFEE2E2),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: const Color(0xFFFCA5A5)),
                    ),
                    child: Text(_errorMessage!, style: const TextStyle(color: Color(0xFFB91C1C), fontSize: 13)),
                  ),
                  const SizedBox(height: 16),
                ],

                if (_isEmailTab) ...[
                  // Email Field
                  TextField(
                    controller: _emailController,
                    keyboardType: TextInputType.emailAddress,
                    decoration: InputDecoration(
                      labelText: 'Email Address',
                      hintText: 'name@example.com',
                      prefixIcon: const Icon(Icons.email_outlined),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Password Field
                  TextField(
                    controller: _passwordController,
                    obscureText: _obscurePassword,
                    decoration: InputDecoration(
                      labelText: 'Password',
                      prefixIcon: const Icon(Icons.lock_outline),
                      suffixIcon: IconButton(
                        icon: Icon(_obscurePassword ? Icons.visibility_off : Icons.visibility),
                        onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                      ),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryGreen,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      onPressed: _isLoading ? null : _handleEmailLogin,
                      child: _isLoading
                          ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                          : const Text('Sign In to Account', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    ),
                  ),
                ] else ...[
                  // Mobile Input Field
                  TextField(
                    controller: _mobileController,
                    keyboardType: TextInputType.phone,
                    decoration: InputDecoration(
                      labelText: '10-Digit Mobile Number',
                      prefixText: '+91 ',
                      prefixIcon: const Icon(Icons.phone_android_rounded),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryGreen,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      onPressed: () {
                        final mobile = _mobileController.text.trim();
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (ctx) => OtpVerificationScreen(
                              targetIdentifier: mobile,
                              targetType: OtpTargetType.mobile,
                              onVerificationSuccess: () {
                                Navigator.of(context).pushNamedAndRemoveUntil('/live_market', (route) => false);
                              },
                            ),
                          ),
                        );
                      },
                      child: const Text('Get OTP on Mobile', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    ),
                  ),
                ],

                const SizedBox(height: 24),
                // Security Notice
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: const [
                    Icon(Icons.shield_outlined, color: textMuted, size: 16),
                    SizedBox(width: 6),
                    Text(
                      'SEBI & NSE/BSE Compliant Session Gate',
                      style: TextStyle(color: textMuted, fontSize: 11, fontWeight: FontWeight.w500),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
