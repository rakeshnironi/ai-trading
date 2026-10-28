from datetime import datetime
from sqlalchemy import (
    Column, Integer, String, Float, Boolean, DateTime, ForeignKey, Index
)
from sqlalchemy.orm import relationship
from ..database.session import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    name = Column(String(120), nullable=False)
    phone = Column(String(30), nullable=True)
    client_code = Column(String(50), nullable=True, default="ANGEL_DEFAULT")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relationships
    watchlist_items = relationship("WatchlistItem", back_populates="user", cascade="all, delete-orphan")
    orders = relationship("Order", back_populates="user", cascade="all, delete-orphan")
    portfolio_items = relationship("PortfolioItem", back_populates="user", cascade="all, delete-orphan")
    transactions = relationship("Transaction", back_populates="user", cascade="all, delete-orphan")
    paper_wallet = relationship("PaperWallet", back_populates="user", uselist=False, cascade="all, delete-orphan")
    broker_connection = relationship("BrokerConnection", back_populates="user", uselist=False, cascade="all, delete-orphan")

class WatchlistItem(Base):
    __tablename__ = "watchlist"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(20), default="NSE")
    token = Column(String(50), nullable=True)
    added_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="watchlist_items")

class PortfolioItem(Base):
    __tablename__ = "portfolio"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(20), default="NSE")
    quantity = Column(Integer, nullable=False, default=0)
    avg_buy_price = Column(Float, nullable=False, default=0.0)
    current_price = Column(Float, nullable=False, default=0.0)
    trading_mode = Column(String(20), default="PAPER") # PAPER or LIVE
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    user = relationship("User", back_populates="portfolio_items")

class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(20), default="NSE")
    side = Column(String(10), nullable=False) # BUY or SELL
    order_type = Column(String(20), default="MARKET") # MARKET, LIMIT, SL
    quantity = Column(Integer, nullable=False)
    price = Column(Float, nullable=True)
    trigger_price = Column(Float, nullable=True)
    status = Column(String(20), default="PENDING") # PENDING, EXECUTED, REJECTED, CANCELLED
    trading_mode = Column(String(20), default="PAPER") # PAPER or LIVE
    broker_order_id = Column(String(100), nullable=True)
    failure_reason = Column(String(255), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="orders")
    transactions = relationship("Transaction", back_populates="order")

class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    order_id = Column(Integer, ForeignKey("orders.id"), nullable=True)
    symbol = Column(String(50), nullable=False, index=True)
    side = Column(String(10), nullable=False) # BUY or SELL
    quantity = Column(Integer, nullable=False)
    executed_price = Column(Float, nullable=False)
    total_amount = Column(Float, nullable=False)
    pnl = Column(Float, default=0.0) # Realized profit/loss on sales
    trading_mode = Column(String(20), default="PAPER") # PAPER or LIVE
    timestamp = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="transactions")
    order = relationship("Order", back_populates="transactions")

class PaperWallet(Base):
    __tablename__ = "paper_wallet"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True, nullable=False)
    initial_balance = Column(Float, default=1000000.0)
    current_balance = Column(Float, default=1000000.0) # ₹10,00,000 virtual money
    reserved_margin = Column(Float, default=0.0)
    total_realized_pnl = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    user = relationship("User", back_populates="paper_wallet")

class Candle(Base):
    __tablename__ = "candles"

    id = Column(Integer, primary_key=True, index=True)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(20), default="NSE")
    timeframe = Column(String(20), default="1D") # 1m, 5m, 15m, 1h, 1D
    timestamp = Column(DateTime, nullable=False, index=True)
    open = Column(Float, nullable=False)
    high = Column(Float, nullable=False)
    low = Column(Float, nullable=False)
    close = Column(Float, nullable=False)
    volume = Column(Float, default=0.0)

    __table_args__ = (
        Index("idx_candle_sym_tf_ts", "symbol", "timeframe", "timestamp"),
    )

class BrokerConnection(Base):
    __tablename__ = "broker_connections"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True, nullable=False)
    broker_name = Column(String(50), default="ANGEL_ONE") # ANGEL_ONE, ZERODHA, UPSTOX, GROWW
    client_code = Column(String(50), nullable=False)
    api_key = Column(String(100), nullable=True)
    jwt_token = Column(String(500), nullable=True)
    feed_token = Column(String(500), nullable=True)
    connection_status = Column(String(30), default="NOT_CONNECTED") # NOT_CONNECTED, CONNECTING, CONNECTED, EXPIRED, ERROR
    last_error_message = Column(String(255), nullable=True)
    connected_at = Column(DateTime, nullable=True)
    expires_at = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    user = relationship("User", back_populates="broker_connection")

class TokenBlacklist(Base):
    __tablename__ = "token_blacklist"

    id = Column(Integer, primary_key=True, index=True)
    token = Column(String(500), unique=True, index=True, nullable=False)
    revoked_at = Column(DateTime, default=datetime.utcnow)

