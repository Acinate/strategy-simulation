# StrategySimulation
This strategy uses a Triple-OCO Bracket Order. See: https://tlc.thinkorswim.com/center/howToTos/thinkManual/Trade/Active-Trader/AT-Entering-Orders

Stop loss is set to 10% for all contracts (we use 15% for slippage / market orders)
Take profits are set at 75%, 100%, and 125% which gives us around a 25% - 100% ROC

We start with an account size of $250. Increasing this initial balance doesn't improve results (actually makes it worse)

Anytime we reach a new profitLevel (e.g. 1000, 2000, etc.) we take half of that value and add permanently to our bank-roll

After reaching this profitLevel, we increase our level to the next. (e.g. tradeBalance hit 1000, now we target 2000)

The only time we return to a previous level is when the tradeBalance falls below $80. This is where we have to add more money from our bank-roll. We always only add $250 as this tradeBalance is very volatile.

This protects our accounts from the inevitable draw-downs. Worst case scenario we lose 10 trades in a row and lose $250. However, we still have a massive bank-roll that will supply us with unlimited $250 initial balances.

## Todo
* Extend Strategy object to create FuturesStrategy object
* Add setCommissionAmount function
* Add Market Data to back test on. Random trades on 1 minute chart.
