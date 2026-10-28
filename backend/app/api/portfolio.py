from typing import Optional
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from ..database.session import get_db
from ..models.database import User, PortfolioItem
from ..models.schemas import PortfolioSummaryResponse, PortfolioItemResponse
from ..api.auth import get_current_user
from ..websocket.stream import ws_manager

router = APIRouter(prefix="/portfolio", tags=["Portfolio"])

@router.get("", response_model=PortfolioSummaryResponse)
def get_portfolio(
    mode: Optional[str] = "PAPER",
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    query = db.query(PortfolioItem).filter(
        PortfolioItem.user_id == current_user.id,
        PortfolioItem.trading_mode == mode.upper()
    )
    items = query.all()

    response_items = []
    total_invested = 0.0
    total_current_value = 0.0

    for item in items:
        # Update with live tick price
        live_price = ws_manager.get_current_price(item.symbol)
        item.current_price = live_price

        invested = round(item.quantity * item.avg_buy_price, 2)
        curr_val = round(item.quantity * live_price, 2)
        unrealized = round(curr_val - invested, 2)
        unrealized_pct = round((unrealized / invested) * 100, 2) if invested > 0 else 0.0

        total_invested += invested
        total_current_value += curr_val

        response_items.append(
            PortfolioItemResponse(
                id=item.id,
                symbol=item.symbol,
                exchange=item.exchange,
                quantity=item.quantity,
                avg_buy_price=item.avg_buy_price,
                current_price=live_price,
                invested_value=invested,
                current_value=curr_val,
                unrealized_pnl=unrealized,
                unrealized_pnl_pct=unrealized_pct,
                trading_mode=item.trading_mode
            )
        )

    db.commit() # Save refreshed current prices

    total_unrealized = round(total_current_value - total_invested, 2)
    total_unrealized_pct = round((total_unrealized / total_invested) * 100, 2) if total_invested > 0 else 0.0

    return PortfolioSummaryResponse(
        items=response_items,
        total_invested=round(total_invested, 2),
        total_current_value=round(total_current_value, 2),
        total_unrealized_pnl=total_unrealized,
        total_unrealized_pnl_pct=total_unrealized_pct,
        trading_mode=mode.upper()
    )
