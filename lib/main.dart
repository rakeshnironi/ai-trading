// lib/main.dart
import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/live_market_screen.dart';
import 'screens/otp_verification_screen.dart';
import 'models/auth_models.dart';
import 'services/token_storage_service.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const AutonomousTradingApp());
}

class AutonomousTradingApp extends StatelessWidget {
  const AutonomousTradingApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    const primaryGreen = Color(0xFF00D09C);
    final isAuthenticated = TokenStorageService().isAuthenticated;

    return MaterialApp(
      title: 'Autonomous Trading App',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: Colors.white,
        colorScheme: ColorScheme.fromSeed(
          seedColor: primaryGreen,
          primary: primaryGreen,
          surface: Colors.white,
          brightness: Brightness.light,
        ),
        fontFamily: 'Roboto',
      ),
      initialRoute: isAuthenticated ? '/live_market' : '/login',
      routes: {
        '/login': (context) => const LoginScreen(),
        '/live_market': (context) => const LiveMarketScreen(),
        '/home': (context) => const LiveMarketScreen(),
        '/otp': (context) => const OtpVerificationScreen(
              targetIdentifier: '9876543210',
              targetType: OtpTargetType.mobile,
            ),
      },
    );
  }
}
