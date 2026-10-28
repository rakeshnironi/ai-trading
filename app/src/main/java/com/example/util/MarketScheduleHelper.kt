package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Indian Stock Market (NSE / BSE) Trading Sessions
 */
enum class MarketSession {
    PRE_MARKET,           // 09:00 AM - 09:15 AM IST (Mon-Fri)
    NORMAL_TRADING_OPEN,  // 09:15 AM - 03:30 PM IST (Mon-Fri)
    POST_MARKET,          // 03:30 PM - 04:00 PM IST (Mon-Fri)
    AFTER_HOURS_CLOSED,   // 04:00 PM - 09:00 AM IST (Mon-Fri, AMO eligible)
    WEEKEND_CLOSED,       // Saturday & Sunday
    HOLIDAY_CLOSED        // National & Exchange Trading Holidays
}

/**
 * Exchange Holiday Data Model
 */
data class ExchangeHoliday(
    val date: String,       // "YYYY-MM-DD"
    val dayOfWeek: String,  // "Monday", etc.
    val festivalName: String,
    val exchangesAffected: String = "NSE & BSE",
    val isMuhuratTrading: Boolean = false
)

/**
 * Market Status Information Object
 */
data class MarketStatusInfo(
    val session: MarketSession,
    val isOpen: Boolean,
    val isHoliday: Boolean,
    val isWeekend: Boolean,
    val statusLabel: String,
    val badgeColorHex: Long, // 0xFF00C853 (Green), 0xFFFFB300 (Amber), 0xFFFF5252 (Red)
    val holidayName: String? = null,
    val nextOpenTimeText: String,
    val tradingAdvice: String,
    val currentTimeIst: String
)

/**
 * Indian Stock Exchange Market Schedule & Holiday Helper
 * Follows official NSE & BSE India trading timings in IST (Asia/Kolkata).
 */
object MarketScheduleHelper {

    private val IST_TIMEZONE = TimeZone.getTimeZone("Asia/Kolkata")

    // Official NSE / BSE Trading Holidays for 2025 & 2026
    val HOLIDAYS_LIST: List<ExchangeHoliday> = listOf(
        // 2026 Official NSE / BSE Calendar
        ExchangeHoliday("2026-01-26", "Monday", "Republic Day"),
        ExchangeHoliday("2026-02-17", "Tuesday", "Mahashivratri"),
        ExchangeHoliday("2026-03-03", "Tuesday", "Holi"),
        ExchangeHoliday("2026-03-20", "Friday", "Id-Ul-Fitr (Ramzan Id)"),
        ExchangeHoliday("2026-03-27", "Friday", "Shri Ram Navami"),
        ExchangeHoliday("2026-04-03", "Friday", "Good Friday"),
        ExchangeHoliday("2026-04-14", "Tuesday", "Dr. Baba Saheb Ambedkar Jayanti"),
        ExchangeHoliday("2026-05-01", "Friday", "Maharashtra Day"),
        ExchangeHoliday("2026-05-27", "Wednesday", "Bakri Id (Eid-Ul-Adha)"),
        ExchangeHoliday("2026-06-25", "Thursday", "Muharram"),
        ExchangeHoliday("2026-08-15", "Saturday", "Independence Day"),
        ExchangeHoliday("2026-09-14", "Monday", "Milad-un-Nabi"),
        ExchangeHoliday("2026-10-02", "Friday", "Mahatma Gandhi Jayanti"),
        ExchangeHoliday("2026-10-20", "Tuesday", "Dussehra"),
        ExchangeHoliday("2026-11-08", "Sunday", "Diwali (Laxmi Pujan)*", isMuhuratTrading = true),
        ExchangeHoliday("2026-11-10", "Tuesday", "Diwali Balipratipada"),
        ExchangeHoliday("2026-11-24", "Tuesday", "Guru Nanak Jayanti"),
        ExchangeHoliday("2026-12-25", "Friday", "Christmas")
    )

    /**
     * Compute current market status based on Asia/Kolkata (IST) time
     */
    fun getMarketStatus(timestamp: Long = System.currentTimeMillis()): MarketStatusInfo {
        val cal = Calendar.getInstance(IST_TIMEZONE)
        cal.timeInMillis = timestamp

        val timeSdf = SimpleDateFormat("hh:mm a", Locale.US).apply { timeZone = IST_TIMEZONE }
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = IST_TIMEZONE }

        val todayDateStr = dateSdf.format(cal.time)
        val currentTimeStr = timeSdf.format(cal.time)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val minuteOfDay = hour * 60 + minute

        // 1. Check if today is a Declared Exchange Holiday
        val matchedHoliday = HOLIDAYS_LIST.find { it.date == todayDateStr }
        if (matchedHoliday != null) {
            val nextOpen = calculateNextMarketOpen(cal)
            return MarketStatusInfo(
                session = MarketSession.HOLIDAY_CLOSED,
                isOpen = false,
                isHoliday = true,
                isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY),
                statusLabel = "Holiday Closed",
                badgeColorHex = 0xFFFFB300, // Amber
                holidayName = matchedHoliday.festivalName,
                nextOpenTimeText = nextOpen,
                tradingAdvice = "Market closed for ${matchedHoliday.festivalName}. Real orders can be placed as AMO (After Market Orders).",
                currentTimeIst = "$currentTimeStr IST"
            )
        }

        // 2. Check if today is Weekend (Saturday or Sunday)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            val weekendDay = if (dayOfWeek == Calendar.SATURDAY) "Saturday" else "Sunday"
            val nextOpen = calculateNextMarketOpen(cal)
            return MarketStatusInfo(
                session = MarketSession.WEEKEND_CLOSED,
                isOpen = false,
                isHoliday = true,
                isWeekend = true,
                statusLabel = "Weekend Closed",
                badgeColorHex = 0xFFFFB300, // Amber
                holidayName = "$weekendDay (NSE/BSE Closed)",
                nextOpenTimeText = nextOpen,
                tradingAdvice = "Market is closed for the weekend. Orders will queue as After Market Orders (AMO) for Monday opening bell.",
                currentTimeIst = "$currentTimeStr IST"
            )
        }

        // 3. Weekday Timing Checks (09:00 - 15:30 IST)
        // 09:00 AM = 540 min, 09:15 AM = 555 min, 15:30 (3:30 PM) = 930 min, 16:00 (4:00 PM) = 960 min
        return when {
            minuteOfDay in 540 until 555 -> {
                // Pre-market session (09:00 - 09:15 AM)
                MarketStatusInfo(
                    session = MarketSession.PRE_MARKET,
                    isOpen = false,
                    isHoliday = false,
                    isWeekend = false,
                    statusLabel = "Pre-Market",
                    badgeColorHex = 0xFF2979FF, // Blue
                    holidayName = null,
                    nextOpenTimeText = "Opens at 09:15 AM IST (Today)",
                    tradingAdvice = "Pre-market order matching session active. Normal trading commences at 09:15 AM IST.",
                    currentTimeIst = "$currentTimeStr IST"
                )
            }
            minuteOfDay in 555 until 930 -> {
                // Normal trading hours (09:15 AM - 03:30 PM)
                MarketStatusInfo(
                    session = MarketSession.NORMAL_TRADING_OPEN,
                    isOpen = true,
                    isHoliday = false,
                    isWeekend = false,
                    statusLabel = "Market Open",
                    badgeColorHex = 0xFF00C853, // Emerald Green
                    holidayName = null,
                    nextOpenTimeText = "Closes today at 03:30 PM IST",
                    tradingAdvice = "Live trading active on NSE & BSE. Real-time SmartAPI feeds connected.",
                    currentTimeIst = "$currentTimeStr IST"
                )
            }
            minuteOfDay in 930 until 960 -> {
                // Post-market session (03:30 PM - 04:00 PM)
                MarketStatusInfo(
                    session = MarketSession.POST_MARKET,
                    isOpen = false,
                    isHoliday = false,
                    isWeekend = false,
                    statusLabel = "Post-Market",
                    badgeColorHex = 0xFFFFB300, // Amber
                    holidayName = null,
                    nextOpenTimeText = calculateNextMarketOpen(cal),
                    tradingAdvice = "Post-market closing session. Closing prices finalized.",
                    currentTimeIst = "$currentTimeStr IST"
                )
            }
            else -> {
                // After hours (04:00 PM - 09:00 AM)
                val nextOpen = calculateNextMarketOpen(cal)
                MarketStatusInfo(
                    session = MarketSession.AFTER_HOURS_CLOSED,
                    isOpen = false,
                    isHoliday = false,
                    isWeekend = false,
                    statusLabel = "Market Closed",
                    badgeColorHex = 0xFFFF5252, // Muted Red / Coral
                    holidayName = null,
                    nextOpenTimeText = nextOpen,
                    tradingAdvice = "Trading closed for the day. Place AMO (After Market Orders) queued for next session.",
                    currentTimeIst = "$currentTimeStr IST"
                )
            }
        }
    }

    /**
     * Compute next trading day 09:15 AM IST
     */
    private fun calculateNextMarketOpen(fromCal: Calendar): String {
        val next = fromCal.clone() as Calendar
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = IST_TIMEZONE }
        val daySdf = SimpleDateFormat("EEEE", Locale.US).apply { timeZone = IST_TIMEZONE }

        // Advance day by day until we find a weekday that is not a holiday
        var safetyCounter = 0
        while (safetyCounter++ < 14) {
            val hour = next.get(Calendar.HOUR_OF_DAY)
            val minute = next.get(Calendar.MINUTE)
            val minuteOfDay = hour * 60 + minute

            // If we are on the same day and it's before 09:15 AM and it's a weekday and not a holiday, opens today
            val todayDayOfWeek = next.get(Calendar.DAY_OF_WEEK)
            val todayStr = dateSdf.format(next.time)
            val isHoliday = HOLIDAYS_LIST.any { it.date == todayStr }

            if (safetyCounter == 1 && minuteOfDay < 555 && todayDayOfWeek in Calendar.MONDAY..Calendar.FRIDAY && !isHoliday) {
                return "Today at 09:15 AM IST"
            }

            // Otherwise check next day
            next.add(Calendar.DAY_OF_MONTH, 1)
            val nextDow = next.get(Calendar.DAY_OF_WEEK)
            val nextDateStr = dateSdf.format(next.time)
            val isNextHoliday = HOLIDAYS_LIST.any { it.date == nextDateStr }

            if (nextDow in Calendar.MONDAY..Calendar.FRIDAY && !isNextHoliday) {
                val dayName = daySdf.format(next.time)
                return "$dayName at 09:15 AM IST"
            }
        }
        return "Monday at 09:15 AM IST"
    }
}
