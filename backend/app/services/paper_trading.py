from datetime import datetime
from typing import Dict, Any, Tuple
from sqlalchemy.orm import Session
from ..models.database import User, PaperWallet, Order, PortfolioItem, Transaction

class PaperTradingEngine:
    """
    Virtual Execution Engine.
    Simulates real-world order matching, margin checking, weighted average cost accounting,
    realized/unrealized P&L calculations, and ₹10,00,000 virtual wallet balance management.
    """

    @classmethod
    def get_or_create_wallet(cls, db: Session, user_id: int) -> PaperWallet:
        wallet = db.query(PaperWallet).filter(PaperWallet.user_id == user_id).first()
        if not wallet:
            wallet = PaperWallet(
                user_id=user_id,
                initial_balance=1000000.0, # ₹10,00,000 virtual balance
                current_balance=1000000.0,
                reserved_margin=0.0,
                total_realized_pnl=0.0
            )
            db.add(wallet)
            db.commit()
            db.refresh(wallet)
        return wallet

    @classmethod
    def execute_order(
        cls,
        db: Session,
        user: User,
        order: Order,
        current_market_price: float
    ) -> Tuple[bool, str, Dict[str, Any]]:
        """
        Executes an order in Paper Trading Mode.
        """
        execution_price = order.price if (order.order_type == "LIMIT" and order.price) else current_market_price
        if execution_price <= 0:
            execution_price = current_market_price

        wallet = cls.get_or_create_wallet(db, user.id)
        total_trade_value = execution_price * order.quantity

        if order.side == "BUY":
            # Check available funds
            if wallet.current_balance < total_trade_value:
                order.status = "REJECTED"
                order.failure_reason = f"Insufficient paper wallet balance. Required ₹{total_trade_value:,.2f}, Available ₹{wallet.current_balance:,.2f}"
                db.commit()
                return False, order.failure_reason, {}

            # Deduct wallet balance
            wallet.current_balance -= total_trade_value
            wallet.updated_at = datetime.utcnow()

            # Update or create portfolio holding
            portfolio_item = db.query(PortfolioItem).filter(
                PortfolioItem.user_id == user.id,
                PortfolioItem.symbol == order.symbol,
                PortfolioItem.trading_mode == "PAPER"
            ).first()

            if portfolio_item:
                old_total_cost = portfolio_item.quantity * portfolio_item.avg_buy_price
                new_quantity = portfolio_item.quantity + order.quantity
                new_avg_price = (old_total_cost + total_trade_value) / new_quantity
                portfolio_item.quantity = new_quantity
                portfolio_item.avg_buy_price = round(new_avg_price, 2)
                portfolio_item.current_price = execution_price
                portfolio_item.updated_at = datetime.utcnow()
            else:
                portfolio_item = PortfolioItem(
                    user_id=user.id,
                    symbol=order.symbol,
                    exchange=order.exchange,
                    quantity=order.quantity,
                    avg_buy_price=round(execution_price, 2),
                    current_price=execution_price,
                    trading_mode="PAPER"
                )
                db.add(portfolio_item)

            # Record Transaction
            transaction = Transaction(
                user_id=user.id,
                order_id=order.id,
                symbol=order.symbol,
                side="BUY",
                quantity=order.quantity,
                executed_price=round(execution_price, 2),
                total_amount=round(total_trade_value, 2),
                pnl=0.0,
                trading_mode="PAPER"
            )
            db.add(transaction)

            order.status = "EXECUTED"
            order.price = round(execution_price, 2)
            db.commit()
            db.refresh(order)

            return True, f"Paper BUY executed: {order.quantity} qty of {order.symbol} @ ₹{execution_price:.2f}", {
                "executed_price": execution_price,
                "quantity": order.quantity,
                "total_value": total_trade_value,
                "remaining_balance": wallet.current_balance
            }

        elif order.side == "SELL":
            # Find existing portfolio position
            portfolio_item = db.query(PortfolioItem).filter(
                PortfolioItem.user_id == user.id,
                PortfolioItem.symbol == order.symbol,
                PortfolioItem.trading_mode == "PAPER"
            ).first()

            if not portfolio_item or portfolio_item.quantity < order.quantity:
                current_qty = portfolio_item.quantity if portfolio_item else 0
                order.status = "REJECTED"
                order.failure_reason = f"Insufficient holdings. You own {current_qty} shares of {order.symbol}, attempted to sell {order.quantity}."
                db.commit()
                return False, order.failure_reason, {}

            # Calculate realized P&L
            cost_basis = portfolio_item.avg_buy_price * order.quantity
            realized_pnl = total_trade_value - cost_basis

            # Credit wallet
            wallet.current_balance += total_trade_value
            wallet.total_realized_pnl += realized_pnl
            wallet.updated_at = datetime.utcnow()

            # Update Portfolio
            remaining_qty = portfolio_item.quantity - order.quantity
            if remaining_qty == 0:
                db.delete(portfolio_item)
            else:
                portfolio_item.quantity = remaining_qty
                portfolio_item.current_price = execution_price
                portfolio_item.updated_at = datetime.utcnow()

            # Record Transaction
            transaction = Transaction(
                user_id=user.id,
                order_id=order.id,
                symbol=order.symbol,
                side="SELL",
                quantity=order.quantity,
                executed_price=round(execution_price, 2),
                total_amount=round(total_trade_value, 2),
                pnl=round(realized_pnl, 2),
                trading_mode="PAPER"
            )
            db.add(transaction)

            order.status = "EXECUTED"
            order.price = round(execution_price, 2)
            db.commit()
            db.refresh(order)

            return True, f"Paper SELL executed: {order.quantity} qty of {order.symbol} @ ₹{execution_price:.2f} (P&L: ₹{realized_pnl:+,.2f})", {
                "executed_price": execution_price,
                "quantity": order.quantity,
                "total_value": total_trade_value,
                "realized_pnl": realized_pnl,
                "new_wallet_balance": wallet.current_balance
            }

        return False, "Unsupported order side", {}
