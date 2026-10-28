// lib/screens/otp_verification_screen.dart
import 'package:flutter/material.dart';
import '../controllers/otp_verification_controller.dart';
import '../models/auth_models.dart';
import '../widgets/otp_digit_box.dart';

/// Groww & Angel One Style Material 3 OTP Verification Screen
/// Responsive across Android, iOS, and Windows Desktop.
class OtpVerificationScreen extends StatefulWidget {
  final String targetIdentifier;
  final OtpTargetType targetType;
  final VoidCallback? onVerificationSuccess;
  final VoidCallback? onChangeContact;

  const OtpVerificationScreen({
    Key? key,
    this.targetIdentifier = '9876543210',
    this.targetType = OtpTargetType.mobile,
    this.onVerificationSuccess,
    this.onChangeContact,
  }) : super(key: key);

  @override
  State<OtpVerificationScreen> createState() => _OtpVerificationScreenState();
}

class _OtpVerificationScreenState extends State<OtpVerificationScreen>
    with SingleTickerProviderStateMixin {
  late OtpVerificationController _controller;
  late AnimationController _animController;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  static const Color primaryGreen = Color(0xFF00D09C);
  static const Color neutralDark = Color(0xFF0F172A);
  static const Color neutralGrey = Color(0xFF64748B);
  static const Color surfaceBorder = Color(0xFFE2E8F0);

  @override
  void initState() {
    super.initState();
    _controller = OtpVerificationController(
      targetIdentifier: widget.targetIdentifier,
      targetType: widget.targetType,
    );
    _controller.addListener(_onControllerStateChanged);

    // Initial focus on the first digit
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_controller.focusNodes.isNotEmpty) {
        _controller.focusNodes[0].requestFocus();
      }
    });

    // Fade and slide entry animation
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );
    _fadeAnimation = CurvedAnimation(
      parent: _animController,
      curve: Curves.easeOutCubic,
    );
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.08),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _animController,
      curve: Curves.easeOutCubic,
    ));

    _animController.forward();
  }

  void _onControllerStateChanged() {
    if (!mounted) return;

    // Show Error SnackBar
    if (_controller.errorMessage != null) {
      ScaffoldMessenger.of(context).hideCurrentSnackBar();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: const Color(0xFFDC2626),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          content: Row(
            children: [
              const Icon(Icons.error_outline, color: Colors.white, size: 20),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  _controller.errorMessage!,
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w500),
                ),
              ),
            ],
          ),
          duration: const Duration(seconds: 4),
        ),
      );
      _controller.errorMessage = null;
    }

    // Show Success SnackBar
    if (_controller.successMessage != null) {
      ScaffoldMessenger.of(context).hideCurrentSnackBar();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: primaryGreen,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          content: Row(
            children: [
              const Icon(Icons.check_circle_outline, color: Colors.white, size: 20),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  _controller.successMessage!,
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
                ),
              ),
            ],
          ),
          duration: const Duration(seconds: 2),
        ),
      );
      _controller.successMessage = null;
    }

    // Navigate to Home on verification
    if (_controller.isVerified) {
      Future.delayed(const Duration(milliseconds: 600), () {
        if (!mounted) return;
        if (widget.onVerificationSuccess != null) {
          widget.onVerificationSuccess!();
        } else {
          Navigator.of(context).pushReplacementNamed('/home');
        }
      });
    }

    setState(() {});
  }

  @override
  void dispose() {
    _controller.removeListener(_onControllerStateChanged);
    _controller.dispose();
    _animController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        scrolledUnderElevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded, color: neutralDark, size: 20),
          onPressed: () {
            if (widget.onChangeContact != null) {
              widget.onChangeContact!();
            } else {
              Navigator.of(context).maybePop();
            }
          },
        ),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16.0),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: FadeTransition(
                opacity: _fadeAnimation,
                child: SlideTransition(
                  position: _slideAnimation,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      // 1. App Logo at the top
                      _buildAppLogo(),

                      const SizedBox(height: 28),

                      // 2. Screen Title
                      const Text(
                        'Verify OTP',
                        style: TextStyle(
                          fontSize: 26,
                          fontWeight: FontWeight.w800,
                          color: neutralDark,
                          letterSpacing: -0.5,
                        ),
                        textAlign: TextAlign.center,
                      ),

                      const SizedBox(height: 10),

                      // 3. Subtitle & Masked Contact
                      RichText(
                        textAlign: TextAlign.center,
                        text: TextSpan(
                          style: const TextStyle(
                            fontSize: 14,
                            color: neutralGrey,
                            height: 1.45,
                          ),
                          children: [
                            const TextSpan(
                              text:
                                  'Enter the 6-digit verification code sent to your registered ',
                            ),
                            TextSpan(
                              text: widget.targetType == OtpTargetType.mobile
                                  ? 'mobile number\n'
                                  : 'email address\n',
                              style: const TextStyle(fontWeight: FontWeight.w500),
                            ),
                            TextSpan(
                              text: _controller.maskedTarget,
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                                color: neutralDark,
                                fontSize: 15,
                              ),
                            ),
                          ],
                        ),
                      ),

                      const SizedBox(height: 36),

                      // 4. Six individual OTP input boxes
                      _buildOtpBoxesRow(),

                      const SizedBox(height: 16),

                      // Attempts remaining indicator if under limit
                      if (_controller.attemptsCount > 0 && _controller.attemptsRemaining > 0)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 8.0),
                          child: Text(
                            '${_controller.attemptsRemaining} of 5 attempts remaining',
                            style: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                              color: Color(0xFFD97706),
                            ),
                          ),
                        ),

                      const SizedBox(height: 24),

                      // 5. Large Verify OTP Button with loading animation
                      _buildVerifyButton(),

                      const SizedBox(height: 24),

                      // 6. Resend OTP Button with 30s Countdown Timer
                      _buildResendSection(),

                      const SizedBox(height: 12),

                      // 7. Change Mobile / Email Button
                      _buildChangeContactButton(),

                      const SizedBox(height: 20),

                      // Security compliance guarantee badge
                      _buildSecurityBadge(),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  /// App Logo (Groww / Angel One inspired trading badge)
  Widget _buildAppLogo() {
    return Container(
      width: 72,
      height: 72,
      decoration: BoxDecoration(
        color: Colors.white,
        shape: BoxShape.circle,
        border: Border.all(color: surfaceBorder, width: 1.5),
        boxShadow: [
          BoxShadow(
            color: neutralDark.withOpacity(0.06),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Center(
        child: Container(
          width: 52,
          height: 52,
          decoration: const BoxDecoration(
            shape: BoxShape.circle,
            gradient: LinearGradient(
              colors: [Color(0xFF00D09C), Color(0xFF00B080)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: const Icon(
            Icons.candlestick_chart_rounded,
            color: Colors.white,
            size: 28,
          ),
        ),
      ),
    );
  }

  /// Row of 6 individual OTP Input Boxes with Auto-next & Backspace
  Widget _buildOtpBoxesRow() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: List.generate(6, (index) {
        return OtpDigitBox(
          index: index,
          controller: _controller.digitControllers[index],
          focusNode: _controller.focusNodes[index],
          primaryGreen: primaryGreen,
          onChanged: (val) => _controller.onDigitChanged(index, val),
          onBackspace: () => _controller.onBackspace(index),
        );
      }),
    );
  }

  /// Large Verify OTP Button (Enabled only when 6 digits are entered)
  Widget _buildVerifyButton() {
    final isEnabled = _controller.isOtpComplete && !_controller.isLoading;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      width: double.infinity,
      height: 52,
      child: ElevatedButton(
        onPressed: isEnabled ? () => _controller.verifyOtp() : null,
        style: ElevatedButton.styleFrom(
          backgroundColor: primaryGreen,
          disabledBackgroundColor: const Color(0xFFE2E8F0),
          disabledForegroundColor: const Color(0xFF94A3B8),
          foregroundColor: Colors.white,
          elevation: isEnabled ? 2 : 0,
          shadowColor: primaryGreen.withOpacity(0.4),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
        child: _controller.isLoading
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                  strokeWidth: 2.5,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              )
            : const Text(
                'Verify OTP',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 0.3,
                ),
              ),
      ),
    );
  }

  /// Resend OTP with 30s countdown timer
  Widget _buildResendSection() {
    final canResend = _controller.canResend && !_controller.isLoading;

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          "Didn't receive code? ",
          style: TextStyle(
            fontSize: 14,
            color: neutralGrey,
          ),
        ),
        TextButton(
          onPressed: canResend ? () => _controller.resendOtp() : null,
          style: TextButton.styleFrom(
            padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
            minimumSize: Size.zero,
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
          ),
          child: Text(
            canResend
                ? 'Resend OTP'
                : 'Resend in ${_controller.resendSecondsLeft}s',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w700,
              color: canResend ? primaryGreen : const Color(0xFF94A3B8),
            ),
          ),
        ),
      ],
    );
  }

  /// Change Mobile/Email Action
  Widget _buildChangeContactButton() {
    final label = widget.targetType == OtpTargetType.mobile
        ? 'Change Mobile Number'
        : 'Change Email Address';

    return TextButton(
      onPressed: () {
        if (widget.onChangeContact != null) {
          widget.onChangeContact!();
        } else {
          Navigator.of(context).maybePop();
        }
      },
      style: TextButton.styleFrom(
        foregroundColor: neutralGrey,
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      ),
      child: Text(
        label,
        style: const TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.w600,
          decoration: TextDecoration.underline,
        ),
      ),
    );
  }

  /// Security Rules compliance caption
  Widget _buildSecurityBadge() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: surfaceBorder),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: const [
          Icon(Icons.shield_outlined, color: primaryGreen, size: 14),
          SizedBox(width: 6),
          Text(
            '256-Bit SSL Encrypted • Valid for 5 min • Max 5 attempts',
            style: TextStyle(
              fontSize: 10,
              fontWeight: FontWeight.w500,
              color: Color(0xFF64748B),
            ),
          ),
        ],
      ),
    );
  }
}
