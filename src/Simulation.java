// This strategy uses a Triple-OCO Bracket Order. See: https://tlc.thinkorswim.com/center/howToTos/thinkManual/Trade/Active-Trader/AT-Entering-Orders
// Stop loss is set to 25% for all contracts (we use 30% for slippage / market orders)
// Take profits are set at 75%, 100%, and 125% which gives us around a 25% - 100% ROC

// We start with an account size of $250. Increasing this initial balance doesn't improve results (actually makes it worse)
// Anytime we reach a new profitLevel (e.g. 1000, 2000, etc.) we take half of that value and add permanently to our bank-roll
// After reaching this profitLevel, we increase our level to the next. (e.g. tradeBalance hit 1000, now we target 2000)

// The only time we return to a previous level is when the tradeBalance falls below $80. This is where we have
// to add more money from our bank-roll. We always only add $250 as this tradeBalance is very volatile.
// This protects our accounts from the inevitable draw-downs. Worst case scenario we lose 10 trades in a row
// and lose $250. However, we still have a massive bank-roll that will supply us with unlimited $250 initial balances.
public class Simulation {
    public static void main(String[] args) {
        Strategy strategy = idealOptionsStrategy();
//        strategy.runSimulation();
        strategy.runManySimulations();
//        numberLossToBankrupt();
//        numberWinsToMillion();
    }

    static Strategy idealOptionsStrategy() {
        Strategy optionStrategy = new Strategy("All-In SPY Options", 250);
        optionStrategy.setRisk(0.05, 0.15);
        optionStrategy.setReward(0.15, 0.45);
        optionStrategy.setWinRate(0.55);
        return optionStrategy;
    }

    static Strategy autoCryptoStrategy() {
        Strategy optionStrategy = new Strategy("Automatic Crypto Strategy", 50);
        optionStrategy.setRisk(0.125, 0.15);
        optionStrategy.setReward(0.45, 0.50);
        optionStrategy.setWinRate(0.28);
        return optionStrategy;
    }

    static void numberLossToBankrupt() {
        for (int i = 5; i <= 30; i += 5) {
            double initialBalance = 250;
            double countUntilBankrupt = 0;
            while (initialBalance >= 50) {
                initialBalance *= 1 - ((double) i / 100);
                countUntilBankrupt++;
            }
            System.out.println("[" + i + "%] # Losses: " + countUntilBankrupt);
        }
    }

    static void numberWinsToMillion() {
        for (int i = 5; i <= 50; i += 5) {
            double initialBalance = 250;
            double countUntilMillion = 0;
            while (initialBalance < 1000000) {
                initialBalance *= 1 + ((double) i / 100);
                countUntilMillion++;
            }
            System.out.println("[" + i + "%] # Wins: " + countUntilMillion);
        }
    }

    static Strategy aggressiveOptionsStrategy() {
        Strategy optionsStrategy = new Strategy("Aggressive All-In SPY Options", 250);
        optionsStrategy.setRisk(0.15, 0.30);
        optionsStrategy.setReward(0.45, 0.90);
        optionsStrategy.setWinRate(0.35);
        return optionsStrategy;
    }

    static Strategy realisticOptionsStrategy() {
        Strategy optionStrategy = new Strategy("All-In SPY Options", 250);
        optionStrategy.setRisk(0.01, 0.20);
        optionStrategy.setReward(0.01, 0.65);
        optionStrategy.setWinRate(0.34);
        return optionStrategy;
    }
}
