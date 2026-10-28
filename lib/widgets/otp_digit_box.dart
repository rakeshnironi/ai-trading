// lib/widgets/otp_digit_box.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Individual digit box for 6-digit Groww & Angel One style OTP input
class OtpDigitBox extends StatelessWidget {
  final int index;
  final TextEditingController controller;
  final FocusNode focusNode;
  final ValueChanged<String> onChanged;
  final VoidCallback onBackspace;
  final Color primaryGreen;

  const OtpDigitBox({
    Key? key,
    required this.index,
    required this.controller,
    required this.focusNode,
    required this.onChanged,
    required this.onBackspace,
    this.primaryGreen = const Color(0xFF00D09C),
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final hasFocus = focusNode.hasFocus;
    final hasContent = controller.text.isNotEmpty;

    return Focus(
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent &&
            event.logicalKey == LogicalKeyboardKey.backspace) {
          onBackspace();
          return KeyEventResult.handled;
        }
        return KeyEventResult.ignored;
      },
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeInOut,
        width: 48,
        height: 56,
        decoration: BoxDecoration(
          color: hasFocus ? primaryGreen.withOpacity(0.04) : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: hasFocus
                ? primaryGreen
                : (hasContent ? const Color(0xFF0F172A) : const Color(0xFFE2E8F0)),
            width: hasFocus ? 2.0 : 1.2,
          ),
          boxShadow: hasFocus
              ? [
                  BoxShadow(
                    color: primaryGreen.withOpacity(0.18),
                    blurRadius: 8,
                    offset: const Offset(0, 2),
                  ),
                ]
              : null,
        ),
        child: Center(
          child: TextField(
            controller: controller,
            focusNode: focusNode,
            textAlign: TextAlign.center,
            keyboardType: TextInputType.number,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(2), // Allow detection of multi-digit / paste
            ],
            style: const TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w700,
              color: Color(0xFF0F172A),
              fontFamily: 'Roboto',
            ),
            cursorColor: primaryGreen,
            decoration: const InputDecoration(
              border: InputBorder.none,
              focusedBorder: InputBorder.none,
              enabledBorder: InputBorder.none,
              errorBorder: InputBorder.none,
              disabledBorder: InputBorder.none,
              contentPadding: EdgeInsets.zero,
              counterText: '',
            ),
            onChanged: (val) => onChanged(val),
          ),
        ),
      ),
    );
  }
}
