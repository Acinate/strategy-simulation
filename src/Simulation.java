// This strategy uses a Triple-OCO Bracket Order. See: https://tlc.thinkorswim.com/center/howToTos/thinkManual/Trade/Active-Trader/AT-Entering-Orders
// Stop loss is set to 25% for all contracts (we use 30% for slippage / market orders)
// Take profits are set at 75%, 100%, and 125% which gives us around a 25% - 100% ROC

// We start with an account size of $250. Increasing this initial balance doesn't improve results (actually makes it worse)
// Anytime we reach a new profitLevel (e.g. 1000, 2000, etc.) we take half of that value and add permanently to our bank-roll
// After reaching this profitLevel, we increase our level to the next. (e.g. tradeBalance hit 1000, now we target 2000)

public class Simulation {
    public static void main(String[] args) {
        Strategy strategy = optionsStrategy();
        strategy.runManySimulations();
        strategy.runSimulation(60);
//        numberLossToBankrupt();
//        numberWinsToMillion();
    }

    static Strategy optionsStrategy() {
        Strategy optionStrategy = new Strategy("Options Trading", 10000);
        optionStrategy.setRisk(0.03, 0.10);
        optionStrategy.setReward(0.05, 0.45);
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
}
