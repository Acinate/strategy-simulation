public class Simulation {
    public static void main(String[] args) {
        Strategy strategy = optionsStrategy();
        strategy.runManySimulations();
        strategy.runSimulation(44);
//        numberLossToBankrupt();
//        numberWinsToMillion();
    }

    static Strategy optionsStrategy() {
        Strategy optionStrategy = new Strategy("Options Trading", 750);
        optionStrategy.setMaxTrades(100);
        optionStrategy.setRisk(0, 0.15);
        optionStrategy.setReward(0, 0.45);
        optionStrategy.setTakePercentageProfits(0.10);
        return optionStrategy;
    }

    static void numberLossToBankrupt() {
        for (int i = 5; i <= 30; i += 5) {
            double initialBalance = 1000;
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
