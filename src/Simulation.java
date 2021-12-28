public class Simulation {
    public static void main(String[] args) {
        Strategy strategy = futuresStrategy();
        strategy.runManySimulations();
        strategy.runSimulation();
//        numberLossToBankrupt();
//        numberWinsToMillion();
    }

    static Strategy optionsStrategy() {
        Strategy optionStrategy = new Strategy("Options Trading", 500);
        optionStrategy.setMaxTrades(200);
        optionStrategy.setWinRate(50);
        optionStrategy.setRisk(0, 0.05);
        optionStrategy.setReward(0, 0.30);
        optionStrategy.setUseBankRoll(false);
//        optionStrategy.setTakePercentageProfits(0.10);
        return optionStrategy;
    }

    static Strategy futuresStrategy() {
        Strategy futuresStrategy = new Strategy("Futures Trading", 500);
        futuresStrategy.setMaxTrades(100);
        futuresStrategy.setWinRate(50);
        futuresStrategy.setRisk(0, 0.05);
        futuresStrategy.setReward(0, 0.125);
        futuresStrategy.setUseBankRoll(false);
        return futuresStrategy;
    }

    static void numberLossToBankrupt() {
        for (int i = 5; i <= 30; i += 5) {
            double initialBalance = 500;
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
            double initialBalance = 1000;
            double countUntilMillion = 0;
            while (initialBalance < 1000000) {
                initialBalance *= 1 + ((double) i / 100);
                countUntilMillion++;
            }
            System.out.println("[" + i + "%] # Wins: " + countUntilMillion);
        }
    }
}
