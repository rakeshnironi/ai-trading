from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, Field

# --- User & Auth Schemas ---
class UserBase(BaseModel):
    email: str
    name: str
    phone: Optional[str] = None
    client_code: Optional[str] = "ANGEL_DEFAULT"

class UserRegister(UserBase):
    password: str

class UserLogin(BaseModel):
    email: str
    password: str

class UserProfileUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    client_code: Optional[str] = None

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse

# --- Watchlist Schemas ---
class WatchlistItemCreate(BaseModel):
    symbol: str
    exchange: Optional[str] = "NSE"
    token: Optional[str] = None

class WatchlistItemResponse(BaseModel):
    id: int
    symbol: str
    exchange: str
    token: Optional[str] = None
    added_at: datetime
    ltp: Optional[float] = None
    change: Optional[float] = None
    change_pct: Optional[float] = None

    class Config:
        from_attributes = True

# --- Order & Trading Schemas ---
class OrderCreate(BaseModel):
    symbol: str
    exchange: Optional[str] = "NSE"
    side: str = Field(..., pattern="^(BUY|SELL)$")
    order_type: Optional[str] = Field("MARKET", pattern="^(MARKET|LIMIT|SL)$")
    quantity: int = Field(..., gt=0)
    price: Optional[float] = None
    trigger_price: Optional[float] = None
    trading_mode: Optional[str] = Field("PAPER", pattern="^(PAPER|LIVE)$")

class OrderResponse(BaseModel):
    id: int
    symbol: str
    exchange: str
    side: str
    order_type: str
    quantity: int
    price: Optional[float] = None
    trigger_price: Optional[float] = None
    status: str
    trading_mode: str
    broker_order_id: Optional[str] = None
    failure_reason: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True

# --- Portfolio & Wallet Schemas ---
class PortfolioItemResponse(BaseModel):
    id: int
    symbol: str
    exchange: str
    quantity: int
    avg_buy_price: float
    current_price: float
    invested_value: float
    current_value: float
    unrealized_pnl: float
    unrealized_pnl_pct: float
    trading_mode: str

    class Config:
        from_attributes = True

class PortfolioSummaryResponse(BaseModel):
    items: List[PortfolioItemResponse]
    total_invested: float
    total_current_value: float
    total_unrealized_pnl: float
    total_unrealized_pnl_pct: float
    trading_mode: str

class WalletResponse(BaseModel):
    trading_mode: str
    current_balance: float
    initial_balance: float
    reserved_margin: float
    total_realized_pnl: float
    available_funds: float

class AddFundsRequest(BaseModel):
    amount: float = Field(..., gt=0)

# --- History & Transaction Schemas ---
class TransactionResponse(BaseModel):
    id: int
    order_id: Optional[int] = None
    symbol: str
    side: str
    quantity: int
    executed_price: float
    total_amount: float
    pnl: float
    trading_mode: str
    timestamp: datetime

    class Config:
        from_attributes = True

# --- Quotes & Candles Schemas ---
class QuoteResponse(BaseModel):
    symbol: str
    exchange: str
    ltp: float
    open: float
    high: float
    low: float
    close: float
    volume: float
    change: float
    change_pct: float
    timestamp: datetime

class CandleResponse(BaseModel):
    timestamp: datetime
    open: float
    high: float
    low: float
    close: float
    volume: float

# --- AI & Indicators Schemas ---
class TechnicalIndicatorsResponse(BaseModel):
    symbol: str
    rsi: float
    ema20: float
    ema50: float
    macd: float
    macd_signal: float
    macd_hist: float
    vwap: float
    atr: float
    signal_bias: str

class AIPredictionResponse(BaseModel):
    symbol: str
    signal: str # BUY, SELL, HOLD
    confidence: float # e.g. 0.86
    confidence_pct: str # e.g. "86%"
    target_price: float
    stop_loss: float
    risk_reward_ratio: str
    indicators: TechnicalIndicatorsResponse
    summary_reasons: List[str]
    timestamp: datetime

# --- Market Status Schemas (Part 3) ---
class MarketStatusResponse(BaseModel):
    exchange: str = "NSE"
    timezone: str = "Asia/Kolkata"
    market_status: str # PRE_MARKET, MARKET_OPEN, MARKET_CLOSED, HOLIDAY
    is_market_open: bool
    session_type: str # REGULAR, PRE_MARKET, SPECIAL
    session_name: Optional[str] = None
    current_time: str # ISO-8601 with timezone
    next_open: Optional[str] = None # ISO-8601 with timezone or null
    next_close: Optional[str] = None # ISO-8601 with timezone or null
    is_holiday: bool
    holiday_name: Optional[str] = None
    data_status: str # REAL_TIME, PRE_MARKET, LAST_AVAILABLE, DELAYED
    last_market_data_timestamp: Optional[str] = None

# --- Broker / Demat Account Schemas (Part 4) ---
class BrokerConnectRequest(BaseModel):
    broker_name: Optional[str] = "ANGEL_ONE"
    client_code: str
    api_key: Optional[str] = None
    mpin_or_password: Optional[str] = None
    totp: Optional[str] = None

class BrokerStatusResponse(BaseModel):
    broker_name: str
    client_code: str
    connection_status: str # NOT_CONNECTED, CONNECTING, CONNECTED, EXPIRED, ERROR
    connected_at: Optional[datetime] = None
    expires_at: Optional[datetime] = None
    last_error_message: Optional[str] = None
    holdings_count: int = 0
    positions_count: int = 0
    available_margin: float = 0.0

class LogoutResponse(BaseModel):
    success: bool = True
    message: str = "Successfully logged out. All credentials and sessions cleared."

