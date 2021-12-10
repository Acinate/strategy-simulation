import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Strategy {
    private String name;
    private double initialBalance;
    private double minLoss = 0.15;
    private double maxLoss = 0.20;
    private double minWin = 0.40;
    private double maxWin = 0.60;
    private double winRate = 0.33;
    private double commission = 0.65;
    private double avgContractCost = 80;
    private boolean logResults = true;
    private boolean useBankRoll = true;

    private int level = 0;
    private int score = 0;
    private double tradeBalance = initialBalance;
    private double bankBalance = 0;
    private double bankruptBalance = 80;
    private double totalCommissionsPaid = 0;
    private double totalLossesIncurred = 0;
    private double totalWinsAccumulated = 0;
    private boolean payCommissionsFromTradeBalance = true;

    private final Random random = new Random();
    private final double[] profitLevels = new double[]{1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000};


    public Strategy(String name, double initialBalance) {
        this.name = name;
        this.initialBalance = initialBalance;
    }

    private void initialize() {
        level = 0;
        score = 0;
        tradeBalance = initialBalance;
        bankBalance = 0;
        totalCommissionsPaid = 0;
        totalLossesIncurred = 0;
        totalWinsAccumulated = 0;
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
        initialize();
        for (int tradeCount = 1; tradeCount <= 500; tradeCount++) {
            if (bankBalance > 10000000 || tradeBalance > 10000000) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }
            if (isWinningTrade()) {
                double max = minMax(minWin, maxWin);
                double percentGain = ThreadLocalRandom.current().nextDouble(minWin, maxWin);
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
                double max = maxLoss - (minMax(minLoss, maxLoss) - minLoss) + 0.01;
                double percentGain = -1 * (ThreadLocalRandom.current().nextDouble(minLoss, maxLoss));
                double netGain = Math.round(tradeBalance * percentGain);
                tradeBalance += netGain;
                totalLossesIncurred += netGain;
                if (logResults) {
                    printTrade(tradeCount, netGain, percentGain, tradeBalance, bankBalance, score, level);
                }
                if (tradeBalance < bankruptBalance) {
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
            if (payCommissionsFromTradeBalance) {
                tradeBalance -= (tradeCommissions * 2);
            } else {
                bankBalance -= (tradeCommissions * 2);
            }
            if (logResults) {
                sleep(1000);
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

    public double runSimulation(int winRate) {
        this.winRate = winRate > 0 ? ((double) winRate / 100) : 0;
        return runSimulation();
    }

    public void runManySimulations() {
        logResults = false;
        double riskPerTrade = (minLoss + maxLoss) / 2;
        double rewardPerTrade = (minWin + maxWin) / 2;
        double riskRewardRatio = Math.round(rewardPerTrade / riskPerTrade);
        System.out.println("Initial Balance: " + printBalance(initialBalance) + " | AVG Risk: " + printPercentage(riskPerTrade) + " | AVG Reward: " + printPercentage(rewardPerTrade) + " | Ratio: " + riskRewardRatio + " | Use Bankroll: " + useBankRoll);
        for (int w = 20; w <= 60; w += 2) {
            int numberTrials = 100;
            double finalBalanceSum = 0, standardDeviation = 0;
            List<Double> finalBalanceList = new ArrayList<>();
            for (int i = 0; i < numberTrials; i++) {
                double finalBalance = runSimulation(w);
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
            System.out.println("WR: " + w + "% | AVG Final Balance: " + printBalance(average));
        }
        logResults = true;
    }

    private boolean isWinningTrade() {
        score = random.nextInt(101);
        return score <= (winRate * 100);
    }

    private final String ANSI_RED = "\u001B[31m";
    private final String ANSI_GREEN = "\u001B[32m";
    private final String ANSI_RESET = "\u001B[0m";

    private void printTrade(int tradeCount, double netGain, double percentGain, double tradeBalance, double bankBalance, double score, double level) {
        String tradeCountStr = "[" + tradeCount + "] " + (netGain > 0 ? ANSI_GREEN + "W" + ANSI_RESET : ANSI_RED + "L" + ANSI_RESET);
        String netGainStr = "Trade Gain: " + printBalance(netGain, percentGain);
        String tradeBalanceStr = "Trade Balance: " + printBalance(tradeBalance);
        String bankBalanceStr = "Bank Balance: " + printBalance(bankBalance);
        String scoreStr = "Total Balance: " + printBalance(tradeBalance + bankBalance);
        String levelStr = "Level: " + level;
        System.out.format("%8s%3s%34s%3s%28s%3s%28s%3s%34s%3s%12s%1s", tradeCountStr, " | ", netGainStr, " | ", tradeBalanceStr, " | ", bankBalanceStr, " | ", scoreStr, " | ", levelStr, "\n");
    }

    private String printBalance(double balance) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        String textColor = balance > 0 ? ANSI_GREEN : ANSI_RED;
        return textColor + numberFormat.format(balance) + ANSI_RESET;
    }

    private String printBalance(double balance, double percentProfit) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        String balanceString = currencyFormat.format(balance);
        String plusStr = balance > 0 ? ANSI_GREEN + "+" : ANSI_RED + "";
        return plusStr + balanceString + " (" + plusStr + percentFormat.format(percentProfit) + ")" + ANSI_RESET;
    }

    private String printPercentage(double percent) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        return percentFormat.format(percent);
    }

    private void sleep(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String calculateROIC(double initialBalance, double finalBalance) {
        double percentChange = Math.round(((finalBalance - initialBalance) / initialBalance * 100));
        return printBalance(finalBalance - initialBalance, percentChange);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public void setRisk(double minLoss, double maxLoss) {
        this.minLoss = minLoss;
        this.maxLoss = maxLoss;
    }

    public void setReward(double minWin, double maxWin) {
        this.minWin = minWin;
        this.maxWin = maxWin;
    }

    public void setWinRate(double winRate) {
        if (winRate > 1) winRate = 1;
        else if (winRate < 0) winRate = 0;
        this.winRate = winRate;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public void setAvgContractCost(double avgContractCost) {
        this.avgContractCost = avgContractCost;
    }

    public void setLogResults(boolean logResults) {
        this.logResults = logResults;
    }

    public void setUseBankRoll(boolean useBankRoll) {
        this.useBankRoll = useBankRoll;
    }

    public void setBankruptBalance(double bankruptBalance) {
        this.bankruptBalance = bankruptBalance;
    }

    public void setPayCommissionsFromTradeBalance(boolean payCommissionsFromTradeBalance) {
        this.payCommissionsFromTradeBalance = payCommissionsFromTradeBalance;
    }
}
