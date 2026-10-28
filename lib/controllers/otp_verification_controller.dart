// lib/controllers/otp_verification_controller.dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/auth_models.dart';
import '../services/auth_api_service.dart';

/// State Controller managing OTP input, focus progression, countdown timers,
/// attempt tracking, and API calls for the Groww / Angel One style screen.
class OtpVerificationController extends ChangeNotifier {
  final AuthApiService _apiService;
  final String targetIdentifier; // e.g. "9876543210" or "rakeshnironi@gmail.com"
  final OtpTargetType targetType;

  // 6 separate text controllers and focus nodes for each OTP digit
  final List<TextEditingController> digitControllers =
      List.generate(6, (_) => TextEditingController());
  final List<FocusNode> focusNodes = List.generate(6, (_) => FocusNode());

  // Timer states
  Timer? _resendTimer;
  Timer? _expiryTimer;
  int resendSecondsLeft = 30;
  int expirySecondsLeft = 300; // 5 minutes validity
  bool get canResend => resendSecondsLeft <= 0;

  // Attempt & Security tracking
  int attemptsCount = 0;
  static const int maxAttempts = 5;
  int get attemptsRemaining => maxAttempts - attemptsCount;

  // UI States
  bool isLoading = false;
  String? errorMessage;
  String? successMessage;
  bool isVerified = false;

  OtpVerificationController({
    required this.targetIdentifier,
    this.targetType = OtpTargetType.mobile,
    AuthApiService? apiService,
  }) : _apiService = apiService ?? AuthApiService() {
    _startResendCountdown();
    _startExpiryCountdown();
  }

  /// Formatted masked target (e.g. "+91 ••••••4821" or "r•••••i@gmail.com")
  String get maskedTarget {
    if (targetType == OtpTargetType.mobile) {
      final clean = targetIdentifier.replaceAll(RegExp(r'\D'), '');
      if (clean.length >= 4) {
        final last4 = clean.substring(clean.length - 4);
        return '+91 ••••••$last4';
      }
      return '+91 ••••••4821';
    } else {
      // Email masking
      final parts = targetIdentifier.split('@');
      if (parts.length == 2 && parts[0].length >= 2) {
        final firstChar = parts[0][0];
        final lastChar = parts[0][parts[0].length - 1];
        return '$firstChar•••••$lastChar@${parts[1]}';
      }
      return 'r•••••i@gmail.com';
    }
  }

  /// Concatenated 6-digit OTP string
  String get currentOtp => digitControllers.map((c) => c.text).join();

  /// True when all 6 digits are provided
  bool get isOtpComplete => currentOtp.length == 6 && RegExp(r'^\d{6}$').hasMatch(currentOtp);

  /// Handle digit input and automatic forward navigation
  void onDigitChanged(int index, String value) {
    if (value.isNotEmpty) {
      // If user pasted a full 6-digit code into any box
      if (value.length > 1) {
        _handlePastedOtp(value);
        return;
      }

      // Ensure single digit only
      final digit = value.substring(value.length - 1);
      digitControllers[index].text = digit;
      digitControllers[index].selection = TextSelection.collapsed(offset: 1);

      // Auto-focus next field
      if (index < 5) {
        focusNodes[index + 1].requestFocus();
      } else {
        // Last digit entered, dismiss keyboard or keep focus
        focusNodes[index].unfocus();
      }
    }
    notifyListeners();
  }

  /// Handle backspace navigation to previous box
  void onBackspace(int index) {
    if (digitControllers[index].text.isEmpty && index > 0) {
      focusNodes[index - 1].requestFocus();
      digitControllers[index - 1].clear();
    } else {
      digitControllers[index].clear();
    }
    notifyListeners();
  }

  /// Support paste of 6-digit code
  void _handlePastedOtp(String pastedText) {
    final digits = pastedText.replaceAll(RegExp(r'\D'), '');
    if (digits.isEmpty) return;

    for (int i = 0; i < 6; i++) {
      if (i < digits.length) {
        digitControllers[i].text = digits[i];
      } else {
        digitControllers[i].clear();
      }
    }
    if (digits.length >= 6) {
      focusNodes[5].unfocus();
    } else {
      focusNodes[digits.length].requestFocus();
    }
    notifyListeners();
  }

  /// 30-Second Countdown for Resend Button
  void _startResendCountdown() {
    _resendTimer?.cancel();
    resendSecondsLeft = 30;
    _resendTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (resendSecondsLeft > 0) {
        resendSecondsLeft--;
        notifyListeners();
      } else {
        timer.cancel();
      }
    });
  }

  /// 5-Minute Expiry Countdown
  void _startExpiryCountdown() {
    _expiryTimer?.cancel();
    expirySecondsLeft = 300;
    _expiryTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (expirySecondsLeft > 0) {
        expirySecondsLeft--;
        notifyListeners();
      } else {
        timer.cancel();
        errorMessage = 'OTP has expired after 5 minutes. Please request a new OTP.';
        notifyListeners();
      }
    });
  }

  /// Resend OTP API Trigger
  Future<void> resendOtp() async {
    if (!canResend || isLoading) return;

    isLoading = true;
    errorMessage = null;
    successMessage = null;
    notifyListeners();

    try {
      if (targetType == OtpTargetType.mobile) {
        await _apiService.sendMobileOtp(targetIdentifier);
      } else {
        await _apiService.sendEmailOtp(targetIdentifier);
      }

      successMessage = 'A new 6-digit OTP has been sent successfully.';
      attemptsCount = 0; // Reset attempts for new OTP cycle
      _clearOtpInputs();
      _startResendCountdown();
      _startExpiryCountdown();
    } catch (e) {
      errorMessage = e.toString().replaceAll('Exception: ', '');
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  /// Verify OTP API Trigger
  Future<bool> verifyOtp() async {
    if (!isOtpComplete || isLoading) return false;

    // Check maximum attempt constraint
    if (attemptsCount >= maxAttempts) {
      errorMessage = 'Maximum verification attempts (5) exceeded. Please resend a new OTP.';
      notifyListeners();
      return false;
    }

    if (expirySecondsLeft <= 0) {
      errorMessage = 'OTP has expired. Please tap Resend OTP.';
      notifyListeners();
      return false;
    }

    isLoading = true;
    errorMessage = null;
    successMessage = null;
    notifyListeners();

    attemptsCount++;

    try {
      AuthResponse response;
      if (targetType == OtpTargetType.mobile) {
        response = await _apiService.verifyMobileOtp(targetIdentifier, currentOtp);
      } else {
        response = await _apiService.verifyEmailOtp(targetIdentifier, currentOtp);
      }

      if (response.success) {
        isVerified = true;
        successMessage = 'Verification successful! Redirecting to Home Dashboard...';
        notifyListeners();
        return true;
      } else {
        errorMessage = response.message ?? 'Verification failed.';
        return false;
      }
    } catch (e) {
      errorMessage = e.toString().replaceAll('Exception: ', '');
      if (attemptsRemaining > 0 && attemptsCount < maxAttempts) {
        errorMessage = '$errorMessage ($attemptsRemaining attempts left)';
      }
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  void _clearOtpInputs() {
    for (var c in digitControllers) {
      c.clear();
    }
    focusNodes[0].requestFocus();
    notifyListeners();
  }

  @override
  void dispose() {
    _resendTimer?.cancel();
    _expiryTimer?.cancel();
    for (var c in digitControllers) {
      c.dispose();
    }
    for (var f in focusNodes) {
      f.dispose();
    }
    super.dispose();
  }
}
