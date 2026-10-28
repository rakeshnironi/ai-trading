import asyncio
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, HTMLResponse
from sqlalchemy.orm import Session

from .database.session import Base, engine, SessionLocal, get_db
from .models.database import User, PaperWallet
from .api import api_router
from .websocket.stream import ws_manager
from .services.smart_api import smart_api_service

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("trading_backend")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1. Initialize SQLite Database Schema
    logger.info("Initializing database tables...")
    Base.metadata.create_all(bind=engine)

    # 2. Seed Default User & ₹10,00,000 Paper Wallet if not exists
    db = SessionLocal()
    try:
        default_user = db.query(User).filter(User.email == "rakeshnironi@gmail.com").first()
        if not default_user:
            default_user = User(
                email="rakeshnironi@gmail.com",
                hashed_password="hashed_placeholder_admin",
                name="Rakesh Nironi",
                phone="+919876543210",
                client_code="ANGEL_RN99"
            )
            db.add(default_user)
            db.commit()
            db.refresh(default_user)

            wallet = PaperWallet(
                user_id=default_user.id,
                initial_balance=1000000.0,
                current_balance=1000000.0,
                reserved_margin=0.0,
                total_realized_pnl=0.0
            )
            db.add(wallet)
            db.commit()
            logger.info("Default user 'Rakesh Nironi' and ₹10,00,000 paper wallet created.")
    except Exception as e:
        logger.error(f"Error seeding default database records: {e}")
    finally:
        db.close()

    # 3. Initialize SmartAPI Service Session
    logger.info("Initializing SmartAPI bridge...")
    smart_api_service.generate_session()

    # 4. Start Background Market-Aware WebSocket Tick Streamer (Idempotent)
    logger.info("Starting background market tick streaming engine (enforcing market hours)...")
    ws_manager.ensure_streaming_started()

    yield

    # Shutdown
    logger.info("Shutting down background services...")
    ws_manager.stop_streaming()

app = FastAPI(
    title="Algorithmic Trading System API",
    description="FastAPI Backend powering Live Market (Angel One SmartAPI) and Paper Trading Engine with AI Indicators and WebSocket streaming.",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for mobile and web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount Central Market Status API (Part 3)
from .services.market_calendar import market_calendar
from .models.schemas import MarketStatusResponse

@app.get("/api/market/status", response_model=MarketStatusResponse, tags=["Market Data"])
def root_market_status():
    """Authoritative Central Market Status endpoint for NSE / BSE."""
    return MarketStatusResponse(**market_calendar.get_market_status())

# Mount REST API Router
app.include_router(api_router)

# Mount direct root-level OTP endpoints requested by clients
from .api.auth import (
    send_mobile_otp, verify_mobile_otp,
    send_email_otp, verify_email_otp,
    SendMobileOtpRequest, VerifyMobileOtpRequest,
    SendEmailOtpRequest, VerifyEmailOtpRequest,
    OtpAuthResponse
)

@app.post("/send-mobile-otp", tags=["OTP Authentication"])
def root_send_mobile_otp(req: SendMobileOtpRequest):
    return send_mobile_otp(req)

@app.post("/verify-mobile-otp", response_model=OtpAuthResponse, tags=["OTP Authentication"])
def root_verify_mobile_otp(req: VerifyMobileOtpRequest, db: Session = Depends(get_db)):
    return verify_mobile_otp(req, db)

@app.post("/send-email-otp", tags=["OTP Authentication"])
def root_send_email_otp(req: SendEmailOtpRequest):
    return send_email_otp(req)

@app.post("/verify-email-otp", response_model=OtpAuthResponse, tags=["OTP Authentication"])
def root_verify_email_otp(req: VerifyEmailOtpRequest, db: Session = Depends(get_db)):
    return verify_email_otp(req, db)


@app.api_route("/health", methods=["GET", "HEAD"], tags=["System"])
def health_check():
    return {"status": "ok", "service": "Autonomous Trading Backend"}

@app.get("/api", tags=["System"])
def api_index():
    return {
        "status": "online",
        "service": "Autonomous Trading & Paper Trading App",
        "version": "1.0.0",
        "endpoints": {
            "docs": "/docs",
            "send_mobile_otp": "/send-mobile-otp",
            "verify_mobile_otp": "/verify-mobile-otp",
            "send_email_otp": "/send-email-otp",
            "verify_email_otp": "/verify-email-otp",
            "auth": "/api/v1/auth",
            "market": "/api/v1/market",
            "watchlist": "/api/v1/watchlist",
            "orders": "/api/v1/orders",
            "portfolio": "/api/v1/portfolio",
            "wallet": "/api/v1/wallet",
            "history": "/api/v1/history",
            "ai": "/api/v1/ai",
            "websocket": "/ws/stream"
        }
    }

@app.api_route("/", methods=["GET", "HEAD"])
def root(request: Request):
    if request.method == "HEAD":
        return HTMLResponse(content="", status_code=200)

    html_content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Autonomous Trading & Paper Trading Platform</title>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg-deep: #080E1E;
      --bg-card: #0F1A34;
      --bg-card-elevated: #152449;
      --border-color: #1E315F;
      --primary-green: #00D09C;
      --danger-red: #FF4D4F;
      --ai-purple: #8B5CF6;
      --angel-orange: #FF5722;
      --text-main: #F1F5F9;
      --text-muted: #94A3B8;
      --accent-cyan: #06B6D4;
      --gold-accent: #F59E0B;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }
    body {
      background: var(--bg-deep);
      color: var(--text-main);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .mono { font-family: 'JetBrains Mono', monospace; }

    /* Top Navigation */
    header {
      background: rgba(15, 26, 52, 0.85);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border-color);
      padding: 12px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .brand-wrap {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .brand-logo {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: linear-gradient(135deg, var(--angel-orange), #FF8A65);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(255, 87, 34, 0.35);
    }
    .brand-logo svg { width: 20px; height: 20px; fill: white; }
    .brand-title { font-size: 16px; font-weight: 800; letter-spacing: -0.3px; }
    .brand-subtitle { font-size: 11px; color: var(--text-muted); font-weight: 500; }

    .header-center {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .mode-pill {
      background: #0B1528;
      border: 1px solid var(--border-color);
      border-radius: 999px;
      padding: 4px 6px;
      display: flex;
      gap: 4px;
    }
    .mode-btn {
      padding: 6px 14px;
      border-radius: 999px;
      border: none;
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
      background: transparent;
      color: var(--text-muted);
    }
    .mode-btn.active {
      background: var(--primary-green);
      color: #051A14;
      box-shadow: 0 2px 8px rgba(0, 208, 156, 0.3);
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: 999px;
      background: rgba(0, 208, 156, 0.12);
      border: 1px solid rgba(0, 208, 156, 0.3);
      color: var(--primary-green);
      font-size: 12px;
      font-weight: 700;
    }
    .status-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: var(--primary-green);
      animation: pulseDot 1.8s infinite;
    }
    @keyframes pulseDot {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(0.85); }
    }
    .user-badge {
      display: flex;
      align-items: center;
      gap: 8px;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      padding: 6px 14px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
    }

    /* Index Strip */
    .indices-strip {
      background: #0B152A;
      border-bottom: 1px solid var(--border-color);
      padding: 8px 24px;
      display: flex;
      gap: 28px;
      overflow-x: auto;
      font-size: 12px;
    }
    .index-item {
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
    }
    .index-name { font-weight: 700; color: var(--text-muted); }
    .index-val { font-weight: 800; }
    .tag-positive { color: var(--primary-green); font-weight: 700; }
    .tag-negative { color: var(--danger-red); font-weight: 700; }

    /* Dashboard Layout */
    .dashboard-grid {
      display: grid;
      grid-template-columns: 280px 1fr 340px;
      gap: 16px;
      padding: 16px 24px;
      flex: 1;
    }
    @media (max-width: 1100px) {
      .dashboard-grid { grid-template-columns: 1fr; }
    }

    .card {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 16px;
      padding: 18px;
      display: flex;
      flex-direction: column;
    }
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 14px;
      padding-bottom: 10px;
      border-bottom: 1px solid rgba(255,255,255,0.06);
    }
    .card-title { font-size: 14px; font-weight: 800; letter-spacing: -0.2px; }

    /* Stock Watchlist */
    .stock-list { display: flex; flex-direction: column; gap: 8px; max-height: 600px; overflow-y: auto; }
    .stock-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      border-radius: 12px;
      background: rgba(255,255,255,0.02);
      border: 1px solid transparent;
      cursor: pointer;
      transition: all 0.15s;
    }
    .stock-row:hover {
      background: var(--bg-card-elevated);
      border-color: rgba(255,255,255,0.1);
    }
    .stock-row.selected {
      background: var(--bg-card-elevated);
      border-color: var(--accent-cyan);
    }
    .stock-sym { font-weight: 800; font-size: 13px; }
    .stock-exch { font-size: 10px; color: var(--text-muted); }
    .stock-price { text-align: right; font-weight: 700; font-size: 13px; }
    .stock-chg { font-size: 11px; font-weight: 600; }

    /* Main Chart Area */
    .chart-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
    }
    .stock-hero-price { font-size: 26px; font-weight: 800; }
    .tf-selector { display: flex; gap: 4px; }
    .tf-btn {
      background: transparent;
      border: 1px solid var(--border-color);
      color: var(--text-muted);
      border-radius: 8px;
      padding: 4px 10px;
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
    }
    .tf-btn.active {
      background: var(--accent-cyan);
      color: #031D24;
      border-color: var(--accent-cyan);
    }
    #chartCanvas {
      width: 100%;
      height: 250px;
      background: #0B1428;
      border-radius: 12px;
      border: 1px solid var(--border-color);
    }

    /* AI Consensus Banner */
    .ai-card {
      margin-top: 14px;
      background: linear-gradient(135deg, rgba(139, 92, 246, 0.12), rgba(6, 182, 212, 0.08));
      border: 1px solid rgba(139, 92, 246, 0.35);
      border-radius: 14px;
      padding: 16px;
    }
    .ai-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
    }
    .signal-pill {
      padding: 4px 14px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 800;
      letter-spacing: 0.5px;
    }
    .signal-buy { background: var(--primary-green); color: #02241A; }
    .signal-sell { background: var(--danger-red); color: white; }
    .signal-hold { background: var(--gold-accent); color: #221400; }

    .ai-metrics {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 8px;
      margin: 12px 0;
    }
    .ai-metric-box {
      background: rgba(15, 26, 52, 0.7);
      border-radius: 10px;
      padding: 8px 10px;
      border: 1px solid rgba(255,255,255,0.06);
    }
    .ai-metric-label { font-size: 10px; color: var(--text-muted); font-weight: 600; text-transform: uppercase; }
    .ai-metric-val { font-size: 13px; font-weight: 800; margin-top: 2px; }

    /* Paper Order Form */
    .wallet-hero {
      background: linear-gradient(135deg, rgba(0, 208, 156, 0.15), rgba(6, 182, 212, 0.08));
      border: 1px solid rgba(0, 208, 156, 0.3);
      border-radius: 14px;
      padding: 16px;
      margin-bottom: 14px;
    }
    .wallet-label { font-size: 11px; font-weight: 700; color: var(--primary-green); text-transform: uppercase; }
    .wallet-val { font-size: 24px; font-weight: 800; color: white; margin-top: 4px; }

    .trade-tabs { display: flex; gap: 8px; margin-bottom: 12px; }
    .trade-tab-btn {
      flex: 1;
      height: 38px;
      border-radius: 10px;
      border: none;
      font-size: 13px;
      font-weight: 800;
      cursor: pointer;
      background: var(--bg-card-elevated);
      color: var(--text-muted);
      transition: all 0.15s;
    }
    .trade-tab-btn.buy-active { background: var(--primary-green); color: #052119; }
    .trade-tab-btn.sell-active { background: var(--danger-red); color: white; }

    .form-group { margin-bottom: 12px; }
    .form-label { font-size: 11px; font-weight: 700; color: var(--text-muted); margin-bottom: 6px; display: block; }
    .form-input {
      width: 100%;
      height: 40px;
      background: #0B1528;
      border: 1px solid var(--border-color);
      border-radius: 10px;
      padding: 0 12px;
      color: white;
      font-weight: 700;
      outline: none;
    }
    .form-input:focus { border-color: var(--primary-green); }

    .qty-presets { display: flex; gap: 6px; margin-top: 6px; }
    .qty-chip {
      background: rgba(255,255,255,0.05);
      border: 1px solid var(--border-color);
      color: var(--text-muted);
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
    }
    .qty-chip:hover { border-color: var(--primary-green); color: white; }

    .execute-btn {
      width: 100%;
      height: 44px;
      border-radius: 12px;
      border: none;
      background: var(--primary-green);
      color: #03241A;
      font-size: 14px;
      font-weight: 800;
      cursor: pointer;
      box-shadow: 0 4px 14px rgba(0, 208, 156, 0.35);
      transition: all 0.2s;
    }
    .execute-btn:hover { filter: brightness(1.08); transform: translateY(-1px); }

    /* Positions Table */
    .positions-wrap { margin-top: 14px; }
    .pos-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px;
      border-radius: 10px;
      background: rgba(255,255,255,0.02);
      border: 1px solid rgba(255,255,255,0.06);
      margin-bottom: 6px;
    }

    /* Floating WhatsApp Alert Banner */
    .wa-banner {
      background: #0C1E18;
      border-top: 1px solid rgba(0, 208, 156, 0.3);
      padding: 10px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 12px;
    }
    .wa-text { display: flex; align-items: center; gap: 8px; }
    .wa-icon { color: #25D366; font-size: 16px; font-weight: bold; }

    /* Modal */
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.7);
      backdrop-filter: blur(6px);
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 100;
    }
    .modal-box {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 20px;
      width: 100%;
      max-width: 420px;
      padding: 28px;
    }
  </style>
</head>
<body>

  <!-- Top Navigation Header -->
  <header>
    <div class="brand-wrap">
      <div class="brand-logo">
        <svg viewBox="0 0 24 24"><path d="M7 3h2v18H7V3zm8 4h2v14h-2V7zm-4 6h2v8h-2v-8z"/></svg>
      </div>
      <div>
        <div class="brand-title">Autonomous Trading & Paper Trading</div>
        <div class="brand-subtitle">SmartAPI Angel One &bull; LSTM AI Engine &bull; Real-Time Analytics</div>
      </div>
    </div>

    <div class="header-center">
      <div class="mode-pill">
        <button id="btnModeLive" class="mode-btn" onclick="setMode('LIVE')">Live Market</button>
        <button id="btnModePaper" class="mode-btn active" onclick="setMode('PAPER')">Paper Trading</button>
      </div>
    </div>

    <div class="header-actions">
      <div class="status-badge">
        <span class="status-dot"></span>
        <span id="marketStatusText">SmartAPI Connected</span>
      </div>
      <button onclick="openOtpModal()" class="mode-btn" style="border: 1px solid var(--border-color); color: var(--text-main);">
        🔑 Test OTP Flow
      </button>
      <div class="user-badge">
        <span>👤 Rakesh Nironi</span>
        <span style="color: var(--primary-green); font-size: 11px;">ANGEL_RN99</span>
      </div>
    </div>
  </header>

  <!-- Indices Live Ticker -->
  <div class="indices-strip">
    <div class="index-item">
      <span class="index-name">NIFTY 50</span>
      <span class="index-val mono">24,350.20</span>
      <span class="tag-positive">+142.30 (+0.59%)</span>
    </div>
    <div class="index-item">
      <span class="index-name">SENSEX</span>
      <span class="index-val mono">80,120.45</span>
      <span class="tag-positive">+410.80 (+0.52%)</span>
    </div>
    <div class="index-item">
      <span class="index-name">BANK NIFTY</span>
      <span class="index-val mono">52,180.90</span>
      <span class="tag-positive">+320.10 (+0.62%)</span>
    </div>
    <div class="index-item">
      <span class="index-name">INDIA VIX</span>
      <span class="index-val mono">12.45</span>
      <span class="tag-negative">-0.35 (-2.73%)</span>
    </div>
  </div>

  <!-- Main Trading Grid -->
  <div class="dashboard-grid">

    <!-- Column 1: Stock Watchlist -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">Market Watchlist (NSE)</div>
        <span style="font-size: 11px; color: var(--text-muted); font-weight: 700;">8 STOCKS</span>
      </div>
      <div class="stock-list" id="stockWatchlist">
        <!-- Injected via JS -->
      </div>
    </div>

    <!-- Column 2: Interactive Chart & AI Consensus Engine -->
    <div class="card">
      <div class="chart-header">
        <div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <h2 id="activeStockSymbol" style="font-size: 20px; font-weight: 800;">RELIANCE</h2>
            <span style="background: rgba(255,255,255,0.08); padding: 2px 8px; border-radius: 6px; font-size: 10px; font-weight: 700;">NSE EQ</span>
          </div>
          <div style="margin-top: 4px; display: flex; align-items: baseline; gap: 10px;">
            <div id="activeStockPrice" class="stock-hero-price mono">₹2,985.40</div>
            <div id="activeStockChange" class="tag-positive mono">+₹42.30 (+1.44%)</div>
          </div>
        </div>

        <div class="tf-selector">
          <button class="tf-btn" onclick="setTimeframe('1D')">1D</button>
          <button class="tf-btn active" onclick="setTimeframe('5D')">5D</button>
          <button class="tf-btn" onclick="setTimeframe('1M')">1M</button>
          <button class="tf-btn" onclick="setTimeframe('1Y')">1Y</button>
        </div>
      </div>

      <!-- Real-time HTML5 Canvas Chart -->
      <canvas id="chartCanvas"></canvas>

      <!-- AI ML Consensus & Technical Indicator Analysis Card -->
      <div class="ai-card">
        <div class="ai-header">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="font-size: 16px;">⚡</span>
            <span style="font-size: 13px; font-weight: 800; color: white;">AI Machine Learning Consensus</span>
          </div>
          <div id="aiSignalBadge" class="signal-pill signal-buy">STRONG BUY (88%)</div>
        </div>

        <p id="aiSummary" style="font-size: 12px; color: var(--text-muted); line-height: 1.5;">
          Price holding above EMA20 & EMA50 golden stack. RSI at 62.4 shows robust bullish expansion. Target ₹3,120.00 with stop-loss at ₹2,910.00.
        </p>

        <div class="ai-metrics">
          <div class="ai-metric-box">
            <div class="ai-metric-label">Target (TP)</div>
            <div id="aiTarget" class="ai-metric-val mono tag-positive">₹3,120.00</div>
          </div>
          <div class="ai-metric-box">
            <div class="ai-metric-label">Stop Loss (SL)</div>
            <div id="aiStopLoss" class="ai-metric-val mono tag-negative">₹2,910.00</div>
          </div>
          <div class="ai-metric-box">
            <div class="ai-metric-label">RSI (14)</div>
            <div id="aiRsi" class="ai-metric-val mono">62.4</div>
          </div>
          <div class="ai-metric-box">
            <div class="ai-metric-label">VWAP Anchor</div>
            <div id="aiVwap" class="ai-metric-val mono">₹2,960.10</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Column 3: Paper Trading Terminal & Holdings -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">Paper Trading Terminal</div>
        <span style="font-size: 11px; color: var(--primary-green); font-weight: 800;">₹10,00,000 SEED</span>
      </div>

      <!-- Virtual Balance Hero -->
      <div class="wallet-hero">
        <div class="wallet-label">Available Virtual Capital</div>
        <div id="walletDisplay" class="wallet-val mono">₹10,00,000.00</div>
        <div style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">
          Realized P&L: <span id="realizedPnlDisplay" class="mono tag-positive">+₹0.00</span>
        </div>
      </div>

      <!-- Trade Form -->
      <div class="trade-tabs">
        <button id="btnSideBuy" class="trade-tab-btn buy-active" onclick="setSide('BUY')">BUY</button>
        <button id="btnSideSell" class="trade-tab-btn" onclick="setSide('SELL')">SELL</button>
      </div>

      <div class="form-group">
        <label class="form-label">Order Type</label>
        <select id="orderType" class="form-input">
          <option value="MARKET">Market Order (Instant Fill)</option>
          <option value="LIMIT">Limit Order</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">Quantity (Shares)</label>
        <input type="number" id="tradeQty" class="form-input mono" value="10" min="1" max="10000" oninput="updateMargin()" />
        <div class="qty-presets">
          <span class="qty-chip" onclick="setQty(5)">+5</span>
          <span class="qty-chip" onclick="setQty(10)">+10</span>
          <span class="qty-chip" onclick="setQty(25)">+25</span>
          <span class="qty-chip" onclick="setQty(50)">+50</span>
          <span class="qty-chip" onclick="setQty(100)">+100</span>
        </div>
      </div>

      <div style="display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 14px;">
        <span style="color: var(--text-muted);">Margin Required:</span>
        <span id="marginReqDisplay" class="mono" style="font-weight: 700;">₹29,854.00</span>
      </div>

      <button id="btnExecuteTrade" class="execute-btn" onclick="executePaperTrade()">
        Execute Paper Order
      </button>

      <!-- Active Paper Holdings -->
      <div class="positions-wrap">
        <div style="font-size: 12px; font-weight: 800; margin-bottom: 8px; color: var(--text-muted); text-transform: uppercase;">
          Virtual Holdings (<span id="holdingsCount">0</span>)
        </div>
        <div id="holdingsList">
          <div style="text-align: center; color: var(--text-muted); font-size: 12px; padding: 18px;">
            No open positions yet. Execute a paper order above!
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- WhatsApp Alert Status Bar -->
  <div class="wa-banner">
    <div class="wa-text">
      <span class="wa-icon">&#128172;</span>
      <span><strong>WhatsApp Trading Alerts:</strong> Active for <strong>+91 9876543210</strong> (Instant notification of AI signals & paper executions)</span>
    </div>
    <button onclick="testWhatsAppAlert()" style="background: rgba(37, 211, 102, 0.15); border: 1px solid rgba(37, 211, 102, 0.4); color: #25D366; padding: 4px 12px; border-radius: 6px; font-size: 11px; font-weight: 700; cursor: pointer;">
      Trigger Test Alert
    </button>
  </div>

  <!-- OTP Verification Modal for Testing Login -->
  <div id="otpModal" class="modal-overlay">
    <div class="modal-box">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <div style="font-weight: 800; font-size: 16px;">Groww & Angel One OTP Verification</div>
        <button onclick="closeOtpModal()" style="background: none; border: none; color: var(--text-muted); font-size: 18px; cursor: pointer;">&times;</button>
      </div>
      <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 16px;">
        Test sending and verifying 6-digit one-time passwords for mobile authentication.
      </p>
      <div class="form-group">
        <label class="form-label">Registered Mobile Number</label>
        <input type="text" id="modalMobile" class="form-input" value="9876543210" />
      </div>
      <div class="form-group">
        <label class="form-label">6-Digit OTP (Pre-filled for testing)</label>
        <input type="text" id="modalOtp" class="form-input mono" value="729410" maxlength="6" />
      </div>
      <div style="display: flex; gap: 8px;">
        <button onclick="sendOtpRequest()" class="mode-btn" style="flex: 1; border: 1px solid var(--border-color); color: var(--text-main); height: 38px;">Send OTP</button>
        <button onclick="verifyOtpRequest()" class="execute-btn" style="flex: 1; height: 38px;">Verify Code</button>
      </div>
      <div id="modalToast" style="margin-top: 12px; font-size: 12px; display: none;"></div>
    </div>
  </div>

  <script>
    // State
    const stocks = [
      { symbol: "RELIANCE", name: "Reliance Industries", price: 2985.40, change: 42.30, changePct: 1.44 },
      { symbol: "TCS", name: "Tata Consultancy Services", price: 3940.10, change: -18.20, changePct: -0.46 },
      { symbol: "INFY", name: "Infosys Limited", price: 1560.80, change: 12.40, changePct: 0.80 },
      { symbol: "HDFCBANK", name: "HDFC Bank Limited", price: 1640.20, change: 8.90, changePct: 0.55 },
      { symbol: "TATAMOTORS", name: "Tata Motors Limited", price: 980.50, change: 24.60, changePct: 2.57 },
      { symbol: "ICICIBANK", name: "ICICI Bank Limited", price: 1180.20, change: -4.30, changePct: -0.36 },
      { symbol: "SBIN", name: "State Bank of India", price: 820.60, change: 11.20, changePct: 1.38 },
      { symbol: "BAJFINANCE", name: "Bajaj Finance Ltd", price: 7120.00, change: 65.00, changePct: 0.92 }
    ];

    let selectedStock = stocks[0];
    let tradeSide = 'BUY';
    let currentMode = 'PAPER';
    let paperBalance = 1000000.00;
    let realizedPnl = 0.00;
    let holdings = [];

    // Render Watchlist
    function renderWatchlist() {
      const container = document.getElementById('stockWatchlist');
      container.innerHTML = stocks.map(s => `
        <div class="stock-row ${s.symbol === selectedStock.symbol ? 'selected' : ''}" onclick="selectStock('${s.symbol}')">
          <div>
            <div class="stock-sym">${s.symbol}</div>
            <div class="stock-exch">${s.name}</div>
          </div>
          <div class="stock-price">
            <div class="mono">₹${s.price.toFixed(2)}</div>
            <div class="stock-chg ${s.change >= 0 ? 'tag-positive' : 'tag-negative'}">
              ${s.change >= 0 ? '+' : ''}${s.change.toFixed(2)} (${s.changePct > 0 ? '+' : ''}${s.changePct.toFixed(2)}%)
            </div>
          </div>
        </div>
      `).join('');
    }

    function selectStock(sym) {
      selectedStock = stocks.find(s => s.symbol === sym) || stocks[0];
      document.getElementById('activeStockSymbol').innerText = selectedStock.symbol;
      document.getElementById('activeStockPrice').innerText = '₹' + selectedStock.price.toFixed(2);
      const chgEl = document.getElementById('activeStockChange');
      chgEl.className = (selectedStock.change >= 0 ? 'tag-positive' : 'tag-negative') + ' mono';
      chgEl.innerText = `${selectedStock.change >= 0 ? '+' : ''}₹${selectedStock.change.toFixed(2)} (${selectedStock.changePct > 0 ? '+' : ''}${selectedStock.changePct.toFixed(2)}%)`;

      // Update AI metrics
      const target = (selectedStock.price * 1.045).toFixed(2);
      const stop = (selectedStock.price * 0.975).toFixed(2);
      document.getElementById('aiTarget').innerText = '₹' + target;
      document.getElementById('aiStopLoss').innerText = '₹' + stop;
      document.getElementById('aiVwap').innerText = '₹' + (selectedStock.price * 0.995).toFixed(2);

      renderWatchlist();
      drawChart();
      updateMargin();
    }

    function setSide(side) {
      tradeSide = side;
      document.getElementById('btnSideBuy').className = 'trade-tab-btn ' + (side === 'BUY' ? 'buy-active' : '');
      document.getElementById('btnSideSell').className = 'trade-tab-btn ' + (side === 'SELL' ? 'sell-active' : '');
      const execBtn = document.getElementById('btnExecuteTrade');
      execBtn.innerText = `Execute Paper ${side} Order`;
      execBtn.style.background = (side === 'BUY' ? 'var(--primary-green)' : 'var(--danger-red)');
      execBtn.style.color = (side === 'BUY' ? '#03241A' : '#ffffff');
    }

    function setQty(q) {
      document.getElementById('tradeQty').value = q;
      updateMargin();
    }

    function updateMargin() {
      const q = parseInt(document.getElementById('tradeQty').value) || 0;
      const margin = q * selectedStock.price;
      document.getElementById('marginReqDisplay').innerText = '₹' + margin.toLocaleString('en-IN', { minimumFractionDigits: 2 });
    }

    function executePaperTrade() {
      const q = parseInt(document.getElementById('tradeQty').value) || 0;
      if (q <= 0) return alert("Enter valid quantity");
      const orderVal = q * selectedStock.price;

      if (tradeSide === 'BUY') {
        if (orderVal > paperBalance) return alert("Insufficient paper balance!");
        paperBalance -= orderVal;

        const existing = holdings.find(h => h.symbol === selectedStock.symbol);
        if (existing) {
          const totalCost = (existing.qty * existing.avg) + orderVal;
          existing.qty += q;
          existing.avg = totalCost / existing.qty;
        } else {
          holdings.push({ symbol: selectedStock.symbol, qty: q, avg: selectedStock.price });
        }
      } else {
        const existing = holdings.find(h => h.symbol === selectedStock.symbol);
        if (!existing || existing.qty < q) return alert("You don't have enough shares to SELL!");
        paperBalance += orderVal;
        const pnl = (selectedStock.price - existing.avg) * q;
        realizedPnl += pnl;
        existing.qty -= q;
        if (existing.qty === 0) {
          holdings = holdings.filter(h => h.symbol !== selectedStock.symbol);
        }
      }

      updateWalletUI();
      renderHoldings();
    }

    function updateWalletUI() {
      document.getElementById('walletDisplay').innerText = '₹' + paperBalance.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      const pnlEl = document.getElementById('realizedPnlDisplay');
      pnlEl.className = (realizedPnl >= 0 ? 'tag-positive' : 'tag-negative') + ' mono';
      pnlEl.innerText = (realizedPnl >= 0 ? '+' : '') + '₹' + realizedPnl.toFixed(2);
    }

    function renderHoldings() {
      document.getElementById('holdingsCount').innerText = holdings.length;
      const list = document.getElementById('holdingsList');
      if (holdings.length === 0) {
        list.innerHTML = '<div style="text-align: center; color: var(--text-muted); font-size: 12px; padding: 18px;">No open positions. Execute a paper order above!</div>';
        return;
      }
      list.innerHTML = holdings.map(h => {
        const st = stocks.find(s => s.symbol === h.symbol) || { price: h.avg };
        const curVal = h.qty * st.price;
        const cost = h.qty * h.avg;
        const pnl = curVal - cost;
        const pnlPct = (pnl / cost) * 100;
        return `
          <div class="pos-item">
            <div>
              <div style="font-weight: 800; font-size: 13px;">${h.symbol}</div>
              <div style="font-size: 11px; color: var(--text-muted);">${h.qty} shares @ ₹${h.avg.toFixed(2)}</div>
            </div>
            <div style="text-align: right;">
              <div class="mono" style="font-weight: 700; font-size: 13px;">₹${curVal.toFixed(2)}</div>
              <div class="${pnl >= 0 ? 'tag-positive' : 'tag-negative'} mono" style="font-size: 11px; font-weight: 700;">
                ${pnl >= 0 ? '+' : ''}₹${pnl.toFixed(2)} (${pnlPct >= 0 ? '+' : ''}${pnlPct.toFixed(2)}%)
              </div>
            </div>
          </div>
        `;
      }).join('');
    }

    function drawChart() {
      const canvas = document.getElementById('chartCanvas');
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      const w = canvas.width = canvas.parentElement.clientWidth - 36;
      const h = canvas.height = 250;

      ctx.clearRect(0, 0, w, h);

      // Draw grid lines
      ctx.strokeStyle = '#152449';
      ctx.lineWidth = 1;
      for (let y = 30; y < h; y += 45) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(w, y);
        ctx.stroke();
      }

      // Generate synthetic candlestick series
      const candleCount = 28;
      const candleW = (w / candleCount) * 0.6;
      const base = selectedStock.price;
      let cur = base * 0.97;

      ctx.lineWidth = 1.5;
      for (let i = 0; i < candleCount; i++) {
        const x = (i + 0.5) * (w / candleCount);
        const delta = (Math.sin(i * 0.6) + (Math.random() - 0.48)) * (base * 0.008);
        const open = cur;
        const close = cur + delta;
        cur = close;
        const high = Math.max(open, close) + Math.random() * (base * 0.005);
        const low = Math.min(open, close) - Math.random() * (base * 0.005);

        const isGreen = close >= open;
        const color = isGreen ? '#00D09C' : '#FF4D4F';

        // Scale to canvas Y
        const minP = base * 0.95;
        const maxP = base * 1.05;
        const scaleY = p => h - 25 - ((p - minP) / (maxP - minP)) * (h - 50);

        // Wick
        ctx.strokeStyle = color;
        ctx.beginPath();
        ctx.moveTo(x, scaleY(high));
        ctx.lineTo(x, scaleY(low));
        ctx.stroke();

        // Body
        ctx.fillStyle = color;
        const topY = scaleY(Math.max(open, close));
        const botY = scaleY(Math.min(open, close));
        ctx.fillRect(x - candleW / 2, topY, candleW, Math.max(botY - topY, 2));
      }

      // EMA Trend Line
      ctx.strokeStyle = '#06B6D4';
      ctx.lineWidth = 2;
      ctx.beginPath();
      for (let i = 0; i < candleCount; i++) {
        const x = (i + 0.5) * (w / candleCount);
        const y = (h * 0.55) - Math.sin(i * 0.3) * 20;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();
    }

    function setTimeframe(tf) {
      document.querySelectorAll('.tf-btn').forEach(b => b.classList.remove('active'));
      event.target.classList.add('active');
      drawChart();
    }

    function setMode(m) {
      currentMode = m;
      document.getElementById('btnModeLive').className = 'mode-btn ' + (m === 'LIVE' ? 'active' : '');
      document.getElementById('btnModePaper').className = 'mode-btn ' + (m === 'PAPER' ? 'active' : '');
    }

    function openOtpModal() { document.getElementById('otpModal').style.display = 'flex'; }
    function closeOtpModal() { document.getElementById('otpModal').style.display = 'none'; }

    async function sendOtpRequest() {
      const mobile = document.getElementById('modalMobile').value;
      const res = await fetch('/send-mobile-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ mobile: mobile })
      });
      const data = await res.json();
      const toast = document.getElementById('modalToast');
      toast.style.display = 'block';
      toast.style.color = '#00D09C';
      toast.innerText = '✓ ' + (data.message || 'OTP sent successfully to ' + mobile);
    }

    async function verifyOtpRequest() {
      const mobile = document.getElementById('modalMobile').value;
      const otp = document.getElementById('modalOtp').value;
      const res = await fetch('/verify-mobile-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ mobile: mobile, otp: otp })
      });
      const data = await res.json();
      const toast = document.getElementById('modalToast');
      toast.style.display = 'block';
      if (data.success) {
        toast.style.color = '#00D09C';
        toast.innerText = '✓ Verified! Session token: ' + (data.access_token ? data.access_token.substring(0, 18) + '...' : 'OK');
        setTimeout(closeOtpModal, 1500);
      } else {
        toast.style.color = '#FF4D4F';
        toast.innerText = '✕ ' + (data.message || 'Invalid OTP code');
      }
    }

    function testWhatsAppAlert() {
      alert("WhatsApp Instant Alert Dispatched!\n\nTo: +91 9876543210\nSignal: STRONG BUY on RELIANCE\nTarget: ₹3,120.00 | Stop-Loss: ₹2,910.00\nExecution Status: Simulated Paper Order Filled");
    }

    // Init
    window.addEventListener('resize', drawChart);
    window.onload = () => {
      renderWatchlist();
      selectStock('RELIANCE');
      updateWalletUI();
      renderHoldings();
    };
  </script>
</body>
</html>
"""
    return HTMLResponse(content=html_content)

@app.websocket("/ws/stream")
async def websocket_endpoint(websocket: WebSocket):
    await ws_manager.connect(websocket)
    try:
        while True:
            # Handle client subscription updates or ping/pong
            data = await websocket.receive_text()
            logger.debug(f"Received client message: {data}")
    except WebSocketDisconnect:
        ws_manager.disconnect(websocket)
    except Exception as e:
        logger.error(f"WebSocket error: {e}")
        ws_manager.disconnect(websocket)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.app.main:app", host="0.0.0.0", port=8000, reload=True)
