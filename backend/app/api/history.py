from typing import List, Optional
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from ..database.session import get_db
from ..models.database import User, Transaction
from ..models.schemas import TransactionResponse
from ..api.auth import get_current_user

router = APIRouter(prefix="/history", tags=["Trade History"])

@router.get("", response_model=List[TransactionResponse])
def get_trade_history(
    mode: Optional[str] = None,
    limit: int = 100,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    query = db.query(Transaction).filter(Transaction.user_id == current_user.id)
    if mode:
        query = query.filter(Transaction.trading_mode == mode.upper())
    return query.order_by(Transaction.timestamp.desc()).limit(limit).all()
