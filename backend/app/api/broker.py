import os
import logging
from datetime import datetime, timedelta
from typing import Dict, Any, Optional, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ..database.session import get_db
from ..models.database import User, BrokerConnection, PortfolioItem, Order, PaperWallet
from ..models.schemas import BrokerConnectRequest, BrokerStatusResponse
from ..api.auth import get_current_user
from ..services.smart_api import smart_api_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/broker", tags=["Broker & Demat Integration"])

@router.get("/status", response_model=BrokerStatusResponse)
def get_broker_status(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Returns the user's authoritative Demat / Broker connection status.
    Strictly isolated to current authenticated user.
    """
    conn = db.query(BrokerConnection).filter(BrokerConnection.user_id == current_user.id).first()

    if not conn:
        return BrokerStatusResponse(
            broker_name="ANGEL_ONE",
            client_code=current_user.client_code or "NOT_CONFIGURED",
            connection_status="NOT_CONNECTED",
            holdings_count=0,
            positions_count=0,
            available_margin=0.0
        )

    # Check if 24-hour token has expired
    if conn.expires_at and datetime.utcnow() > conn.expires_at:
        conn.connection_status = "EXPIRED"
        db.commit()

    holdings_count = 0
    positions_count = 0
    available_margin = 0.0

    if conn.connection_status == "CONNECTED" and conn.jwt_token and conn.api_key:
        # Fetch live stats from Angel One SmartAPI
        margin_res = smart_api_service.fetch_rms_margin(conn.jwt_token, conn.api_key)
        if margin_res.get("status"):
            m_data = margin_res.get("data", {})
            available_margin = float(m_data.get("net", m_data.get("availablecash", 0.0)) or 0.0)

        holdings_res = smart_api_service.fetch_holdings(conn.jwt_token, conn.api_key)
        if holdings_res.get("status"):
            holdings_count = len(holdings_res.get("holdings", []))
        else:
            holdings_count = db.query(PortfolioItem).filter(
                PortfolioItem.user_id == current_user.id,
                PortfolioItem.trading_mode == "LIVE"
            ).count()

        positions_count = db.query(Order).filter(
            Order.user_id == current_user.id,
            Order.trading_mode == "LIVE",
            Order.status == "EXECUTED"
        ).count()

    return BrokerStatusResponse(
        broker_name=conn.broker_name or "ANGEL_ONE",
        client_code=conn.client_code,
        connection_status=conn.connection_status,
        connected_at=conn.connected_at,
        expires_at=conn.expires_at,
        last_error_message=conn.last_error_message,
        holdings_count=holdings_count,
        positions_count=positions_count,
        available_margin=available_margin
    )

@router.post("/connect", response_model=BrokerStatusResponse)
def connect_broker(
    req: BrokerConnectRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Authenticates with supported broker (Angel One SmartAPI V2) using official loginByPassword flow:
    - clientcode: Angel One client ID
    - password: valid trading MPIN
    - totp: active 6-digit TOTP
    """
    broker_name = (req.broker_name or "ANGEL_ONE").upper()
    client_code = req.client_code.strip()

    if not client_code:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Client Code / UCC is required."
        )

    # Find or initialize connection record
    conn = db.query(BrokerConnection).filter(BrokerConnection.user_id == current_user.id).first()
    if not conn:
        conn = BrokerConnection(
            user_id=current_user.id,
            broker_name=broker_name,
            client_code=client_code,
            connection_status="CONNECTING"
        )
        db.add(conn)
    else:
        conn.broker_name = broker_name
        conn.client_code = client_code
        conn.connection_status = "CONNECTING"
        conn.last_error_message = None

    db.commit()

    # Determine effective SmartAPI credentials
    api_key = (req.api_key or os.getenv("SMART_API_KEY", "")).strip()
    password = (req.mpin_or_password or os.getenv("SMART_API_PASSWORD", "")).strip()
    totp = (req.totp or "").strip()

    auth_result = smart_api_service.authenticate_angel_one(
        client_code=client_code,
        password_or_mpin=password,
        totp=totp,
        api_key=api_key
    )

    now = datetime.utcnow()

    if auth_result.get("status"):
        data = auth_result.get("data", {})
        jwt_token = data.get("jwtToken")
        feed_token = data.get("feedToken")

        conn.connection_status = "CONNECTED"
        conn.api_key = api_key
        conn.jwt_token = jwt_token
        conn.feed_token = feed_token
        conn.connected_at = now
        conn.expires_at = now + timedelta(hours=24) # 24-hour token lifecycle
        conn.last_error_message = None
        db.commit()
        db.refresh(conn)

        # Check real available margin and holdings count from broker
        available_margin = 0.0
        margin_res = smart_api_service.fetch_rms_margin(jwt_token, api_key)
        if margin_res.get("status"):
            m_data = margin_res.get("data", {})
            available_margin = float(m_data.get("net", m_data.get("availablecash", 0.0)) or 0.0)

        holdings_res = smart_api_service.fetch_holdings(jwt_token, api_key)
        holdings_count = len(holdings_res.get("holdings", [])) if holdings_res.get("status") else 0

        return BrokerStatusResponse(
            broker_name=broker_name,
            client_code=client_code,
            connection_status="CONNECTED",
            connected_at=conn.connected_at,
            expires_at=conn.expires_at,
            holdings_count=holdings_count,
            positions_count=0,
            available_margin=available_margin
        )
    else:
        error_msg = auth_result.get("message", "Broker authentication rejected.")
        conn.connection_status = "ERROR"
        conn.last_error_message = error_msg
        db.commit()

        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=error_msg
        )

@router.get("/holdings")
def get_broker_holdings(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Retrieves authorized holdings from Angel One SmartAPI for connected user.
    """
    conn = db.query(BrokerConnection).filter(BrokerConnection.user_id == current_user.id).first()
    if not conn or conn.connection_status != "CONNECTED" or not conn.jwt_token:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Broker not connected. Please connect your Angel One Demat account."
        )

    res = smart_api_service.fetch_holdings(conn.jwt_token, conn.api_key)
    return res

@router.post("/disconnect", response_model=BrokerStatusResponse)
def disconnect_broker(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Disconnects broker session for current user without deleting their account.
    """
    conn = db.query(BrokerConnection).filter(BrokerConnection.user_id == current_user.id).first()
    if conn:
        conn.connection_status = "NOT_CONNECTED"
        conn.jwt_token = None
        conn.feed_token = None
        conn.expires_at = None
        conn.last_error_message = None
        db.commit()

    return BrokerStatusResponse(
        broker_name="ANGEL_ONE",
        client_code=current_user.client_code or "NOT_CONNECTED",
        connection_status="NOT_CONNECTED",
        holdings_count=0,
        positions_count=0,
        available_margin=0.0
    )
