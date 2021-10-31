import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulation {
    private static double[] profitLevels = new double[]{1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000};

    public static void main(String[] args) {
//        runManySimulations();
        simulateHomeRunStrategy();
    }

    static void simulateHomeRunStrategy() {
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
        runSimulation(40, 0.25, 1.00, 0.25, 0.30, true);
    }

    static void runManySimulations() {
        for (int w = 30; w <= 50; w += 2) {
            double totalBalance = 0;
            int numberTrials = 100;
            for (int i = 0; i < numberTrials; i++) {
                double finalBalance = runSimulation(w, 0.25, 1.00, 0.25, 0.30, false);
                totalBalance += finalBalance;
            }
            double averageBalance = totalBalance / numberTrials;
            System.out.println("WR: " + w + "% | AVG Final Balance: " + printBalance(averageBalance));
        }
    }

    static double runSimulation(int winRate, double minWin, double maxWin, double minLoss, double maxLoss, boolean logResults) {
        Random random = new Random();
        int tradeCount = 1;
        final double initialBalance = 250;
        double tradeBalance = initialBalance;
        double bankBalance = 0;
        final double commission = 0.65;
        final double avgContractCost = 80;
        double totalCommissionsPaid = 0;
        double totalLossesIncurred = 0;
        int level = 0;
        while (tradeCount <= 500) {
            if (tradeBalance > 10000000 || bankBalance > 10000000) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }
            int randomScore = random.nextInt(100);
            boolean isWinningTrade = randomScore <= winRate;
            String bankBalanceString = printBalance(bankBalance);
            if (isWinningTrade) {
                double profitPercent = random.nextDouble(maxWin - minWin) + minWin;
                double newTradeBalance = Math.round(tradeBalance * (1 + profitPercent));
                tradeBalance = (tradeBalance * profitPercent) + tradeBalance;
                if (logResults) {
                    System.out.println("[" + tradeCount + "] W | Trade Balance: " + printBalance(newTradeBalance, profitPercent) + " | Bank Balance: " + bankBalanceString + " | Score: " + randomScore + " | Level: " + level + " | Commissions: " + printBalance(totalCommissionsPaid) + " | Total Losses: " + printBalance(totalLossesIncurred));
                }
                if (tradeBalance > profitLevels[level]) {
                    bankBalance += profitLevels[level] / 2;
                    tradeBalance -= profitLevels[level] / 2;
                    if (level < profitLevels.length - 1) {
                        level++;
                    }
                }
            } else {
                double profitPercent = -1 * (random.nextDouble(maxLoss - minLoss) + minLoss);
                double tradeLoss = Math.round(tradeBalance * profitPercent);
                tradeBalance += tradeLoss;
                totalLossesIncurred += tradeLoss;
                if (logResults) {
                    System.out.println("[" + tradeCount + "] L | Trade Balance: " + printBalance(tradeBalance, profitPercent) + " | Bank Balance: " + bankBalanceString + " | Score: " + randomScore + " | Level: " + level + " | Commissions: " + printBalance(totalCommissionsPaid) + " | Total Losses: " + printBalance(totalLossesIncurred));
                }
                if (tradeBalance < 80) {
                    bankBalance -= initialBalance;
                    tradeBalance = initialBalance;
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
                    TimeUnit.MILLISECONDS.sleep(250);
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
            System.out.println("===========================================================");
        }
        return finalBalance;
    }

    static String printBalance(double balance) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        return numberFormat.format(balance);
    }

    static String printBalance(double balance, double percentProfit) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        String balanceString = currencyFormat.format(balance);
        return balanceString + " (" + percentFormat.format(percentProfit) + ")";
    }

    static String calculateROIC(double initialBalance, double finalBalance) {
        double percentChange = Math.round(((finalBalance - initialBalance) / initialBalance * 100));
        return printBalance(finalBalance - initialBalance, percentChange);
    }
}
