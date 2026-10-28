from typing import List
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ..database.session import get_db
from ..models.database import User, WatchlistItem
from ..models.schemas import WatchlistItemCreate, WatchlistItemResponse
from ..api.auth import get_current_user
from ..websocket.stream import ws_manager, BASE_PRICES

router = APIRouter(prefix="/watchlist", tags=["Watchlist"])

@router.get("", response_model=List[WatchlistItemResponse])
def get_watchlist(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    items = db.query(WatchlistItem).filter(WatchlistItem.user_id == current_user.id).all()

    # If empty, seed with standard blue chips
    if not items:
        default_symbols = ["RELIANCE", "TCS", "HDFCBANK", "INFY", "TATAMOTORS"]
        for sym in default_symbols:
            item = WatchlistItem(
                user_id=current_user.id,
                symbol=sym,
                exchange="NSE",
                added_at=datetime.utcnow()
            )
            db.add(item)
        db.commit()
        items = db.query(WatchlistItem).filter(WatchlistItem.user_id == current_user.id).all()

    responses = []
    for item in items:
        ltp = ws_manager.get_current_price(item.symbol)
        base = BASE_PRICES.get(item.symbol, ltp)
        change = round(ltp - base, 2)
        change_pct = round((change / base) * 100, 2) if base > 0 else 0.0

        responses.append(
            WatchlistItemResponse(
                id=item.id,
                symbol=item.symbol,
                exchange=item.exchange,
                token=item.token,
                added_at=item.added_at,
                ltp=ltp,
                change=change,
                change_pct=change_pct
            )
        )
    return responses

@router.post("", response_model=WatchlistItemResponse)
def add_to_watchlist(
    item_in: WatchlistItemCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    clean_sym = item_in.symbol.strip().upper()
    existing = db.query(WatchlistItem).filter(
        WatchlistItem.user_id == current_user.id,
        WatchlistItem.symbol == clean_sym
    ).first()

    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"{clean_sym} is already in your watchlist"
        )

    item = WatchlistItem(
        user_id=current_user.id,
        symbol=clean_sym,
        exchange=item_in.exchange or "NSE",
        token=item_in.token,
        added_at=datetime.utcnow()
    )
    db.add(item)
    db.commit()
    db.refresh(item)

    ltp = ws_manager.get_current_price(clean_sym)
    base = BASE_PRICES.get(clean_sym, ltp)
    change = round(ltp - base, 2)
    change_pct = round((change / base) * 100, 2) if base > 0 else 0.0

    return WatchlistItemResponse(
        id=item.id,
        symbol=item.symbol,
        exchange=item.exchange,
        token=item.token,
        added_at=item.added_at,
        ltp=ltp,
        change=change,
        change_pct=change_pct
    )

@router.delete("/{item_id}")
def delete_from_watchlist(
    item_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    item = db.query(WatchlistItem).filter(
        WatchlistItem.id == item_id,
        WatchlistItem.user_id == current_user.id
    ).first()

    if not item:
        raise HTTPException(status_code=404, detail="Watchlist item not found")

    db.delete(item)
    db.commit()
    return {"message": "Symbol removed from watchlist"}
