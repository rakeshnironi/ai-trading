// lib/models/auth_models.dart
import 'dart:convert';

/// Target type for OTP verification: Mobile SMS or Email
enum OtpTargetType {
  mobile,
  email,
}

/// Request payload for sending mobile OTP
class SendMobileOtpRequest {
  final String mobile;

  SendMobileOtpRequest({required this.mobile});

  Map<String, dynamic> toJson() => {
        'mobile': mobile,
      };
}

/// Request payload for verifying mobile OTP
class VerifyMobileOtpRequest {
  final String mobile;
  final String otp;

  VerifyMobileOtpRequest({
    required this.mobile,
    required this.otp,
  });

  Map<String, dynamic> toJson() => {
        'mobile': mobile,
        'otp': otp,
      };
}

/// Request payload for sending email OTP
class SendEmailOtpRequest {
  final String email;

  SendEmailOtpRequest({required this.email});

  Map<String, dynamic> toJson() => {
        'email': email,
      };
}

/// Request payload for verifying email OTP
class VerifyEmailOtpRequest {
  final String email;
  final String otp;

  VerifyEmailOtpRequest({
    required this.email,
    required this.otp,
  });

  Map<String, dynamic> toJson() => {
        'email': email,
        'otp': otp,
      };
}

/// Success authentication response carrying JWT tokens
class AuthResponse {
  final bool success;
  final String accessToken;
  final String refreshToken;
  final int userId;
  final String? message;

  AuthResponse({
    required this.success,
    required this.accessToken,
    required this.refreshToken,
    required this.userId,
    this.message,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      success: json['success'] as bool? ?? false,
      accessToken: json['access_token'] as String? ?? '',
      refreshToken: json['refresh_token'] as String? ?? '',
      userId: (json['user_id'] as num?)?.toInt() ?? 0,
      message: json['message'] as String?,
    );
  }

  Map<String, dynamic> toJson() => {
        'success': success,
        'access_token': accessToken,
        'refresh_token': refreshToken,
        'user_id': userId,
        'message': message,
      };

  @override
  String toString() {
    return 'AuthResponse(success: $success, userId: $userId)';
  }
}
