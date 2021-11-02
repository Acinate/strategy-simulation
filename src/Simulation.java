import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private static double[] profitLevels = new double[]{1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000};

    public static void main(String[] args) {
//        runManySimulations(25000, 0.029, 0.031, 0.009, 0.011, false);
//        runManySimulations(25000, 0.03, 0.09, 0.01, 0.03, false);
//        runManySimulations(25000, 0.06, 0.18, 0.02, 0.06, false);
//        runManySimulations(250, 0.30, 0.75, 0.10, 0.25, true);
//        simulateStockStrategy();
//        simulateOptionsStrategy();
        simulateCryptoStrategy();
//        loseUntilZero();
    }

    static void loseUntilZero() {
        int count = 1;
        double balance = 250;
        while (balance > 100) {
            balance = (balance * 0.80);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[" + count + "]" + printBalance(balance));
            count++;
        }
    }

    static void simulateStockStrategy() {
        runSimulation(25000, 35, 0.03, 0.09, 0.01, 0.03, false, true);
    }

    static void simulateOptionsStrategy() {
        runSimulation(250, 34, 0.15, 0.85, 0.10, 0.25, true, true);
    }

    static void simulateCryptoStrategy() {
        runSimulation(300, 45, 0.50, 0.60, 0.15, 0.20, true, true);
    }

    static void runManySimulations(double initialBalance, double minWin, double maxWin, double minLoss, double maxLoss, boolean useBankRoll) {
        double riskPerTrade = (minLoss + maxLoss) / 2;
        double rewardPerTrade = (minWin + maxWin) / 2;
        double riskRewardRatio = Math.round(rewardPerTrade / riskPerTrade);
        System.out.println("Initial Balance: " + printBalance(initialBalance) + " | AVG Risk: " + printPercentage(riskPerTrade) + " | AVG Reward: " + printPercentage(rewardPerTrade) + " | Ratio: " + riskRewardRatio + " | Use Bankroll: " + useBankRoll);
        for (int w = 20; w <= 40; w += 2) {
            double totalBalance = 0;
            int numberTrials = 100;
            for (int i = 0; i < numberTrials; i++) {
                double finalBalance = runSimulation(initialBalance, w, minWin, maxWin, minLoss, maxLoss, useBankRoll, false);
                totalBalance += finalBalance;
            }
            double averageBalance = totalBalance / numberTrials;
            System.out.println("WR: " + w + "% | AVG Final Balance: " + printBalance(averageBalance));
        }
    }

    static double runSimulation(double initialBalance, int winRate, double minWin, double maxWin, double minLoss, double maxLoss, boolean useBankRoll, boolean logResults) {
        Random random = new Random();
        int tradeCount = 1;
        double tradeBalance = initialBalance;
        double bankBalance = 0;
        final double commission = 0.65;
        final double avgContractCost = 80;
        double totalCommissionsPaid = 0;
        double totalLossesIncurred = 0;
        double totalWinsAccumulated = 0;
        int level = 0;
        while (tradeCount <= 500) {
            if (bankBalance > 10000000 || tradeBalance > 10000000) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }
            int score = random.nextInt(100);
            boolean isWinningTrade = score <= winRate;
            String bankBalanceString = printBalance(bankBalance);
            if (isWinningTrade) {
                double percentGain = random.nextDouble(maxWin - minWin) + minWin;
                double netGain = (tradeBalance * percentGain);
                tradeBalance += netGain;
                totalWinsAccumulated += netGain;
                if (logResults) {
                    printTrade(tradeCount, netGain, percentGain, tradeBalance, bankBalance, score, level);
                }
                if (useBankRoll && tradeBalance > profitLevels[level]) {
                    bankBalance += profitLevels[level] / 2;
                    tradeBalance -= profitLevels[level] / 2;
                    if (level < profitLevels.length - 1) {
                        level++;
                    }
                }
            } else {
                double percentGain = -1 * (random.nextDouble(maxLoss - minLoss) + minLoss);
                double netGain = Math.round(tradeBalance * percentGain);
                tradeBalance += netGain;
                totalLossesIncurred += netGain;
                if (logResults) {
                    printTrade(tradeCount, netGain, percentGain, tradeBalance, bankBalance, score, level);
                }
                if (tradeBalance < 80) {
                    double amtToDeposit = initialBalance - tradeBalance;
                    bankBalance -= amtToDeposit;
                    tradeBalance += amtToDeposit;
                    if (level > 1) {
                        level--;
                    }
                }
            }
            double tradeCommissions = Math.round((tradeBalance / avgContractCost) * commission);
            totalCommissionsPaid += tradeCommissions;
            bankBalance -= tradeCommissions;
            tradeCount++;
            if (logResults) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        double finalBalance = tradeBalance + bankBalance;
        double taxesPaid = finalBalance > 0 ? (finalBalance) * 0.30 : 0;
        double finalBalanceAfterTax = finalBalance - taxesPaid;
        if (logResults) {
            System.out.println("===========================================================");
            System.out.println("Final Balance: " + printBalance(finalBalance) + " | After Taxes: " + printBalance(finalBalanceAfterTax) + " | Taxes Paid: " + printBalance(taxesPaid) + " | ROIC: " + calculateROIC(initialBalance, finalBalanceAfterTax));
            System.out.println("Total Wins: " + printBalance(totalWinsAccumulated) + " | Total Losses: " + printBalance(totalLossesIncurred) + " Total Commissions: " + printBalance(totalCommissionsPaid));
            System.out.println("===========================================================");
        }
        return finalBalanceAfterTax;
    }

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    static void printTrade(int tradeCount, double netGain, double percentGain, double tradeBalance, double bankBalance, double score, double level) {
        String textColor = netGain > 0 ? ANSI_GREEN : ANSI_RED;
        String tradeCountStr = "[" + tradeCount + "] " + (netGain > 0 ? ANSI_GREEN + "W" + ANSI_RESET : ANSI_RED + "L" + ANSI_RESET);
        String netGainStr = "Trade Gain: " + printBalance(netGain, percentGain);
        String tradeBalanceStr = "Trade Balance: " + printBalance(tradeBalance);
        String bankBalanceStr = "Bank Balance: " + printBalance(bankBalance);
        String scoreStr = "Score: " + score;
        String levelStr = "Level: " + level;
        System.out.format("%8s%3s%34s%3s%28s%3s%28s%3s%12s%3s%12s%1s", tradeCountStr, " | ", netGainStr, " | ", tradeBalanceStr, " | ", bankBalanceStr, " | ", scoreStr, " | ", levelStr, "\n");
    }

    static String printBalance(double balance) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        return numberFormat.format(balance);
    }

    static String printBalance(double balance, double percentProfit) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        String balanceString = currencyFormat.format(balance);
        String plusStr = balance > 0 ? ANSI_GREEN + "+" : ANSI_RED + "";
        return plusStr + balanceString + " (" + plusStr + percentFormat.format(percentProfit) + ")" + ANSI_RESET;
    }

    static String printPercentage(double percent) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        return percentFormat.format(percent);
    }

    static String calculateROIC(double initialBalance, double finalBalance) {
        double percentChange = Math.round(((finalBalance - initialBalance) / initialBalance * 100));
        return printBalance(finalBalance - initialBalance, percentChange);
    }
}
