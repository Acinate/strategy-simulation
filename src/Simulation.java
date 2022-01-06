public class Simulation {
    public static void main(String[] args) {
        Strategy strategy = cryptoStrategy();
        strategy.runManySimulations();
        strategy.runSimulation();
//        numberLossToBankrupt();
//        numberWinsToMillion();
    }

    static Strategy cryptoStrategy() {
        Strategy optionStrategy = new CryptoStrategy("Crypto Trading", 500);
        optionStrategy.setMaxTrades(100);
        optionStrategy.setWinRate(50);
        return optionStrategy;
    }

    static Strategy futuresStrategy() {
        Strategy futuresStrategy = new FuturesStrategy("Futures Trading", 500);
        futuresStrategy.setMaxTrades(200);
        futuresStrategy.setWinRate(65);
        futuresStrategy.setUseBankRoll(false);
        return futuresStrategy;
    }

    static void numberLossToBankrupt() {
        for (int i = 1; i <= 15; i += 1) {
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
            while (initialBalance < 500) {
                initialBalance *= 1 + ((double) i / 100);
                countUntilMillion++;
            }
            System.out.println("[" + i + "%] # Wins: " + countUntilMillion);
        }
    }
}
