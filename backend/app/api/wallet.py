from typing import Optional
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from ..database.session import get_db
from ..models.database import User, PaperWallet
from ..models.schemas import WalletResponse, AddFundsRequest
from ..api.auth import get_current_user
from ..services.paper_trading import PaperTradingEngine

router = APIRouter(prefix="/wallet", tags=["Wallet"])

@router.get("", response_model=WalletResponse)
def get_wallet(
    mode: Optional[str] = "PAPER",
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = PaperTradingEngine.get_or_create_wallet(db, current_user.id)
    return WalletResponse(
        trading_mode=mode.upper(),
        current_balance=round(wallet.current_balance, 2),
        initial_balance=round(wallet.initial_balance, 2),
        reserved_margin=round(wallet.reserved_margin, 2),
        total_realized_pnl=round(wallet.total_realized_pnl, 2),
        available_funds=round(wallet.current_balance - wallet.reserved_margin, 2)
    )

@router.post("/add_funds", response_model=WalletResponse)
def add_funds(
    req: AddFundsRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = PaperTradingEngine.get_or_create_wallet(db, current_user.id)
    wallet.current_balance += req.amount
    db.commit()
    db.refresh(wallet)

    return WalletResponse(
        trading_mode="PAPER",
        current_balance=round(wallet.current_balance, 2),
        initial_balance=round(wallet.initial_balance, 2),
        reserved_margin=round(wallet.reserved_margin, 2),
        total_realized_pnl=round(wallet.total_realized_pnl, 2),
        available_funds=round(wallet.current_balance - wallet.reserved_margin, 2)
    )

@router.post("/reset", response_model=WalletResponse)
def reset_wallet(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Resets paper wallet back to the default ₹10,00,000."""
    wallet = PaperTradingEngine.get_or_create_wallet(db, current_user.id)
    wallet.current_balance = 1000000.0
    wallet.initial_balance = 1000000.0
    wallet.reserved_margin = 0.0
    wallet.total_realized_pnl = 0.0
    db.commit()
    db.refresh(wallet)

    return WalletResponse(
        trading_mode="PAPER",
        current_balance=1000000.0,
        initial_balance=1000000.0,
        reserved_margin=0.0,
        total_realized_pnl=0.0,
        available_funds=1000000.0
    )
