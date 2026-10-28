from fastapi import APIRouter
from .auth import router as auth_router
from .market import router as market_router
from .watchlist import router as watchlist_router
from .orders import router as orders_router
from .portfolio import router as portfolio_router
from .wallet import router as wallet_router
from .history import router as history_router
from .ai import router as ai_router
from .broker import router as broker_router

api_router = APIRouter(prefix="/api/v1")
api_router.include_router(auth_router)
api_router.include_router(market_router)
api_router.include_router(broker_router)
api_router.include_router(watchlist_router)
api_router.include_router(orders_router)
api_router.include_router(portfolio_router)
api_router.include_router(wallet_router)
api_router.include_router(history_router)
api_router.include_router(ai_router)

__all__ = ["api_router"]
