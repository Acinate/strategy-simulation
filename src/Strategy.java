import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Strategy {
    String name;
    double initialBalance;
    double winRate = Double.NaN;
    double commission = Double.NaN;
    double taxRate = 0.30;
    boolean logResults = true;
    boolean payCommissionsFromTradeBalance = true;

    int maxTrades = 500;
    int level = 0;
    int score = 0;
    double tradeBalance = initialBalance;
    double bankBalance = 0;
    double bankruptBalance = 0;
    double totalCommissionsPaid = 0;
    double totalLossesIncurred = 0;
    double totalWinsAccumulated = 0;
    double takePercentProfitsPercent = 0;

    final Random random = new Random();
    final double[] profitLevels = new double[]{10000, 50000, 100000, 250000};


    public Strategy(String name, double initialBalance) {
        this.name = name;
        this.initialBalance = initialBalance;
    }

    void resetBalances() {
        level = calculateLevel(initialBalance);
        tradeBalance = initialBalance;
        score = 0;
        bankBalance = 0;
        totalCommissionsPaid = 0;
        totalLossesIncurred = 0;
        totalWinsAccumulated = 0;
    }

    int calculateLevel(double initialBalance) {
        if (initialBalance > profitLevels[0]) {
            for (int i=0; i<profitLevels.length; i++) {
                if (initialBalance <= profitLevels[i]) {
                    return i+1;
                }
            }
        }
        return 0;
    }

    public double minMax(double min, double max) {
        int minInt = (int) (min * 100);
        int maxInt = (int) (max * 100);
        int maxRoll = minInt;
        for (int i = minInt; i < maxInt; i++) {
            maxRoll++;
            if (ThreadLocalRandom.current().nextInt(minInt, maxRoll) < (maxRoll + 1) / 2) {
                break;
            }
        }
        return ((double) maxRoll / 100);
    }

    public double runSimulation() {
        return -1;
    }

    public double runSimulation(int winRate) {
        this.winRate = winRate > 0 ? ((double) winRate / 100) : 0;
        return runSimulation();
    }

    public void runManySimulations() {
        double oldWinRate = this.winRate;
        logResults = false;
        for (int w = 20; w <= 60; w += 2) {
            int numberTrials = 100;
            double finalBalanceSum = 0, standardDeviation = 0;
            List<Double> finalBalanceList = new ArrayList<>();
            for (int i = 0; i < numberTrials; i++) {
                double finalBalance = runSimulation(w);
                if (finalBalance == -1) {
                    System.out.println("Initial balance not high enough for risk employed.");
                    return;
                }
                finalBalanceList.add(finalBalance);
                finalBalanceSum += finalBalance;
            }
            double averageFinalBalance = finalBalanceSum / numberTrials;
            for (double finalBalance : finalBalanceList) {
                standardDeviation += Math.pow(finalBalance - averageFinalBalance, 2);
            }
            Collections.sort(finalBalanceList);
            double median = finalBalanceList.get(finalBalanceList.size() / 2 - 1);
            standardDeviation = Math.sqrt(standardDeviation / numberTrials);
            double finalStandardDeviation = standardDeviation;
            double average = finalBalanceList.stream().filter(fb -> (fb > (median + finalStandardDeviation) || fb < (median + finalStandardDeviation))).reduce(0.0, Double::sum) / numberTrials;
            String roicStr = calculateROIC(initialBalance, average);
            System.out.println("WR: " + w + "% | AVG Final Balance: " + roicStr );
        }
        logResults = true;
        this.winRate = oldWinRate;
    }

    boolean isWinningTrade() {
        score = random.nextInt(101);
        return score <= (winRate * 100);
    }

    boolean isMaxBalanceReached() {
        return (bankBalance * (1 - taxRate)) > 10000000 || (tradeBalance * (1 - taxRate)) > 10000000;
    }

    final String ANSI_RED = "\u001B[31m";
    final String ANSI_GREEN = "\u001B[32m";
    final String ANSI_RESET = "\u001B[0m";

    void printTrade(int tradeCount, double netGain, double rValue, double tradeBalance) {
        String tradeCountStr = "[" + tradeCount + "] " + (netGain > 0 ? ANSI_GREEN + "W" + ANSI_RESET : ANSI_RED + "L" + ANSI_RESET);
        String tradeGainStr = "Trade Gain: " + printBalance(netGain) + " " + printRatio(rValue);
        String accountBalanceStr = "Account Balance: " + printBalance(tradeBalance, (tradeBalance - initialBalance) / initialBalance);
        String commissionsPaidStr = "Commissions paid: " + printBalance(-1 * commission);
        System.out.format("%8s%3s%24s%3s%28s%3s%24s%1s", tradeCountStr, " | ", tradeGainStr, " | ", accountBalanceStr, " | ", commissionsPaidStr, "\n");
    }

    void printTrade(int tradeCount, double netGain, double rValue, double tradeBalance, double numberContracts) {
        String tradeCountStr = "[" + tradeCount + "] " + (netGain > 0 ? ANSI_GREEN + "W" + ANSI_RESET : ANSI_RED + "L" + ANSI_RESET);
        String tradeGainStr = "Trade Gain: " + printBalance(netGain) + " " + printRatio(rValue);
        String accountBalanceStr = "Account Balance: " + printBalance(tradeBalance, (tradeBalance - initialBalance) / initialBalance);
        String numberContractsStr = "# Contracts: " + (int) Math.floor(numberContracts);
        String commissionsPaidStr = "Commissions paid: " + printBalance(-1 * numberContracts * commission * 2);
        System.out.format("%8s%3s%24s%3s%28s%3s%14s%3s%24s%1s", tradeCountStr, " | ", tradeGainStr, " | ", accountBalanceStr, " | ", numberContractsStr, " | ", commissionsPaidStr, "\n");
    }

    String printBalance(double balance) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        String textColor = balance > 0 ? ANSI_GREEN : ANSI_RED;
        return textColor + numberFormat.format(balance) + ANSI_RESET;
    }

    String printBalance(double balance, double percentProfit) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        String balanceString = currencyFormat.format(balance);
        String plusBalanceStr = balance > 0 ? ANSI_GREEN + "+" : ANSI_RED + "";
        String balanceStr = plusBalanceStr + balanceString + ANSI_RESET;
        String plusPercentStr = percentProfit > 0 ? ANSI_GREEN + "(+" : ANSI_RED + "(";
        String percentStr = plusPercentStr + percentFormat.format(percentProfit) + ")" + ANSI_RESET;
        return balanceStr + " " + percentStr;
    }

    String printRatio(double rValue) {
        String rValueStr = rValue > 0 ? ANSI_GREEN + "(+" : ANSI_RED + "(" ;
        return rValueStr + rValue + "R)" + ANSI_RESET;
    }

    String printPercentage(double percent) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        return percentFormat.format(percent);
    }

    void sleep(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String calculateROIC(double initialBalance, double finalBalance) {
        double percentChange = ((finalBalance - initialBalance) / initialBalance);
        return printBalance(finalBalance - initialBalance, percentChange);
    }

    public void setMaxTrades(int maxTrades) {
        this.maxTrades = maxTrades;
    }

    public void setWinRate(double winRate) {
        if (winRate >= 1) this.winRate = (winRate / 100);
        else if (winRate < 0) this.winRate = 0;
    }
}
