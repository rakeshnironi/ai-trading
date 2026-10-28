import os
from datetime import datetime, time, date
from typing import Dict, Any, Optional, Tuple
from zoneinfo import ZoneInfo

# Authoritative Indian Exchange Timezone
INDIA_TZ = ZoneInfo("Asia/Kolkata")

# Official NSE / BSE Standard Trading Hours (IST)
PRE_MARKET_START = time(9, 0)
PRE_MARKET_END = time(9, 15)
REGULAR_MARKET_START = time(9, 15)
REGULAR_MARKET_END = time(15, 30)

# Official NSE / BSE Trading Holidays (2024 - 2026)
NSE_HOLIDAYS = {
    # 2024
    date(2024, 1, 22): "Special Holiday - Ram Mandir Pran Pratishtha",
    date(2024, 1, 26): "Republic Day",
    date(2024, 3, 8): "Mahashivratri",
    date(2024, 3, 25): "Holi",
    date(2024, 3, 29): "Good Friday",
    date(2024, 4, 11): "Id-Ul-Fitr (Ramzan Id)",
    date(2024, 4, 17): "Ram Navami",
    date(2024, 5, 1): "Maharashtra Day",
    date(2024, 5, 20): "Lok Sabha General Elections (Mumbai)",
    date(2024, 6, 17): "Bakri Id",
    date(2024, 7, 17): "Moharram",
    date(2024, 8, 15): "Independence Day",
    date(2024, 10, 2): "Mahatma Gandhi Jayanti",
    date(2024, 11, 1): "Diwali Laxmi Pujan (Muhurat Trading)",
    date(2024, 11, 15): "Gurunanak Jayanti",
    date(2024, 11, 20): "Maharashtra Assembly Election",
    date(2024, 12, 25): "Christmas",
    # 2025
    date(2025, 2, 26): "Mahashivratri",
    date(2025, 3, 14): "Holi",
    date(2025, 3, 31): "Id-Ul-Fitr",
    date(2025, 4, 10): "Mahavir Jayanti",
    date(2025, 4, 14): "Dr. Baba Saheb Ambedkar Jayanti",
    date(2025, 4, 18): "Good Friday",
    date(2025, 5, 1): "Maharashtra Day",
    date(2025, 8, 15): "Independence Day",
    date(2025, 8, 27): "Ganesh Chaturthi",
    date(2025, 10, 2): "Mahatma Gandhi Jayanti",
    date(2025, 10, 21): "Diwali Laxmi Pujan (Muhurat Trading)",
    date(2025, 10, 22): "Diwali Balipratipada",
    date(2025, 11, 5): "Gurunanak Jayanti",
    date(2025, 12, 25): "Christmas",
    # 2026
    date(2026, 1, 26): "Republic Day",
    date(2026, 3, 3): "Holi",
    date(2026, 3, 20): "Id-Ul-Fitr",
    date(2026, 4, 3): "Good Friday",
    date(2026, 4, 14): "Dr. Ambedkar Jayanti",
    date(2026, 5, 1): "Maharashtra Day",
    date(2026, 8, 15): "Independence Day",
    date(2026, 10, 2): "Mahatma Gandhi Jayanti",
    date(2026, 11, 8): "Diwali Laxmi Pujan (Muhurat Trading)",
    date(2026, 11, 24): "Gurunanak Jayanti",
    date(2026, 12, 25): "Christmas",
}

# Special Trading Sessions (e.g. Annual Diwali Muhurat Trading: 18:00 to 19:15 IST)
SPECIAL_SESSIONS: Dict[date, Tuple[time, time, str]] = {
    date(2024, 11, 1): (time(18, 0), time(19, 15), "Diwali Muhurat Trading"),
    date(2025, 10, 21): (time(18, 0), time(19, 15), "Diwali Muhurat Trading"),
    date(2026, 11, 8): (time(18, 0), time(19, 15), "Diwali Muhurat Trading"),
}

class MarketCalendarService:
    """
    Authoritative Market Calendar for NSE / BSE India.
    Calculates market session state strictly according to official exchange hours
    in Asia/Kolkata timezone.
    """

    @staticmethod
    def get_current_ist_time() -> datetime:
        return datetime.now(INDIA_TZ)

    @classmethod
    def get_market_status(cls, current_dt: Optional[datetime] = None) -> Dict[str, Any]:
        """
        Evaluates authoritative market status for NSE/BSE.
        States returned:
          - 'PRE_MARKET' (09:00 - 09:15 IST on trading days)
          - 'MARKET_OPEN' (09:15 - 15:30 IST on trading days or during special sessions)
          - 'MARKET_CLOSED' (After 15:30 IST or before 09:00 IST)
          - 'HOLIDAY' (Official exchange holidays or weekends)
        """
        now = current_dt.astimezone(INDIA_TZ) if current_dt else cls.get_current_ist_time()
        current_date = now.date()
        current_time = now.time()

        is_weekend = now.weekday() >= 5 # 5=Saturday, 6=Sunday
        holiday_name = NSE_HOLIDAYS.get(current_date)
        is_holiday = holiday_name is not None or is_weekend

        # 1. Check for Special Scheduled Sessions (e.g. Diwali Muhurat Trading)
        if current_date in SPECIAL_SESSIONS:
            s_start, s_end, s_name = SPECIAL_SESSIONS[current_date]
            if s_start <= current_time <= s_end:
                return {
                    "exchange": "NSE",
                    "timezone": "Asia/Kolkata",
                    "market_status": "MARKET_OPEN",
                    "is_market_open": True,
                    "session_type": "SPECIAL",
                    "session_name": s_name,
                    "current_time": now.isoformat(),
                    "next_open": None,
                    "next_close": datetime.combine(current_date, s_end, tzinfo=INDIA_TZ).isoformat(),
                    "is_holiday": False,
                    "holiday_name": None,
                    "data_status": "REAL_TIME",
                    "last_market_data_timestamp": now.isoformat()
                }

        # 2. Check for Weekend / Holiday
        if is_holiday:
            next_open = cls._find_next_trading_open(now)
            reason = holiday_name if holiday_name else ("Saturday Weekend" if now.weekday() == 5 else "Sunday Weekend")
            return {
                "exchange": "NSE",
                "timezone": "Asia/Kolkata",
                "market_status": "HOLIDAY",
                "is_market_open": False,
                "session_type": "REGULAR",
                "session_name": reason,
                "current_time": now.isoformat(),
                "next_open": next_open.isoformat() if next_open else None,
                "next_close": None,
                "is_holiday": True,
                "holiday_name": reason,
                "data_status": "LAST_AVAILABLE",
                "last_market_data_timestamp": None
            }

        # 3. Pre-Market Session: 09:00 AM to 09:15 AM IST
        if PRE_MARKET_START <= current_time < PRE_MARKET_END:
            next_open = datetime.combine(current_date, REGULAR_MARKET_START, tzinfo=INDIA_TZ)
            return {
                "exchange": "NSE",
                "timezone": "Asia/Kolkata",
                "market_status": "PRE_MARKET",
                "is_market_open": False,
                "session_type": "PRE_MARKET",
                "session_name": "NSE Pre-Market Session",
                "current_time": now.isoformat(),
                "next_open": next_open.isoformat(),
                "next_close": None,
                "is_holiday": False,
                "holiday_name": None,
                "data_status": "PRE_MARKET",
                "last_market_data_timestamp": now.isoformat()
            }

        # 4. Regular Market Session: 09:15 AM to 15:30 PM IST
        if REGULAR_MARKET_START <= current_time <= REGULAR_MARKET_END:
            next_close = datetime.combine(current_date, REGULAR_MARKET_END, tzinfo=INDIA_TZ)
            return {
                "exchange": "NSE",
                "timezone": "Asia/Kolkata",
                "market_status": "MARKET_OPEN",
                "is_market_open": True,
                "session_type": "REGULAR",
                "session_name": "Normal Equity Trading Session",
                "current_time": now.isoformat(),
                "next_open": None,
                "next_close": next_close.isoformat(),
                "is_holiday": False,
                "holiday_name": None,
                "data_status": "REAL_TIME",
                "last_market_data_timestamp": now.isoformat()
            }

        # 5. Market Closed (Before 09:00 IST or After 15:30 IST on a normal weekday)
        next_open = cls._find_next_trading_open(now)
        return {
            "exchange": "NSE",
            "timezone": "Asia/Kolkata",
            "market_status": "MARKET_CLOSED",
            "is_market_open": False,
            "session_type": "REGULAR",
            "session_name": "Post-Market Session Closed",
            "current_time": now.isoformat(),
            "next_open": next_open.isoformat() if next_open else None,
            "next_close": None,
            "is_holiday": False,
            "holiday_name": None,
            "data_status": "LAST_AVAILABLE",
            "last_market_data_timestamp": None
        }

    @classmethod
    def _find_next_trading_open(cls, from_dt: datetime) -> datetime:
        """Finds the next upcoming market open timestamp in Asia/Kolkata."""
        check_dt = from_dt
        # If today is a trading day and it's before 09:15 AM
        if check_dt.weekday() < 5 and check_dt.date() not in NSE_HOLIDAYS and check_dt.time() < REGULAR_MARKET_START:
            return datetime.combine(check_dt.date(), REGULAR_MARKET_START, tzinfo=INDIA_TZ)

        # Advance day-by-day
        from datetime import timedelta
        for day_offset in range(1, 15):
            candidate_date = (from_dt + timedelta(days=day_offset)).date()
            if candidate_date.weekday() >= 5: # Saturday or Sunday
                continue
            if candidate_date in NSE_HOLIDAYS:
                continue
            return datetime.combine(candidate_date, REGULAR_MARKET_START, tzinfo=INDIA_TZ)

        return from_dt + timedelta(days=1)

market_calendar = MarketCalendarService()
