from typing import List, Optional
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ..database.session import get_db
from ..models.database import User, Order
from ..models.schemas import OrderCreate, OrderResponse
from ..api.auth import get_current_user
from ..services.paper_trading import PaperTradingEngine
from ..services.smart_api import smart_api_service
from ..websocket.stream import ws_manager

router = APIRouter(prefix="/orders", tags=["Orders"])

@router.post("", response_model=OrderResponse)
def place_order(
    order_in: OrderCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    clean_sym = order_in.symbol.strip().upper()
    current_price = ws_manager.get_current_price(clean_sym)

    # 1. Create order record
    order = Order(
        user_id=current_user.id,
        symbol=clean_sym,
        exchange=order_in.exchange or "NSE",
        side=order_in.side,
        order_type=order_in.order_type or "MARKET",
        quantity=order_in.quantity,
        price=order_in.price or current_price,
        trigger_price=order_in.trigger_price,
        status="PENDING",
        trading_mode=order_in.trading_mode or "PAPER",
        created_at=datetime.utcnow()
    )
    db.add(order)
    db.commit()
    db.refresh(order)

    # 2. Route based on Trading Mode
    if order.trading_mode == "PAPER":
        success, message, details = PaperTradingEngine.execute_order(
            db=db,
            user=current_user,
            order=order,
            current_market_price=current_price
        )
        if not success:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=message
            )
    else:
        # LIVE MODE: Route to SmartAPI
        smart_res = smart_api_service.place_live_order({
            "variety": "NORMAL",
            "tradingsymbol": clean_sym,
            "symboltoken": "0",
            "transactiontype": order.side,
            "exchange": order.exchange,
            "ordertype": order.order_type,
            "producttype": "INTRADAY",
            "duration": "DAY",
            "price": str(order.price),
            "squareoff": "0",
            "stoploss": "0",
            "quantity": str(order.quantity)
        })
        if smart_res.get("status"):
            order.status = "EXECUTED"
            order.broker_order_id = str(smart_res.get("order_id", ""))
            db.commit()
        else:
            order.status = "REJECTED"
            order.failure_reason = smart_res.get("message", "SmartAPI execution error")
            db.commit()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=order.failure_reason
            )

    return order

@router.get("", response_model=List[OrderResponse])
def get_orders(
    mode: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    query = db.query(Order).filter(Order.user_id == current_user.id)
    if mode:
        query = query.filter(Order.trading_mode == mode.upper())
    return query.order_by(Order.created_at.desc()).all()
