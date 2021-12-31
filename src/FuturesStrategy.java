import java.util.concurrent.ThreadLocalRandom;

public class FuturesStrategy extends Strategy {

    double contractPrice = 5;
    double pointsRisk = 4;
    double accountRisk = 0.10;
    double[] rMultipleProfitArr = {2};
    double[] rMultipleLossArr = {1};
    double commission = 0.25;

    public FuturesStrategy(String name, double initialBalance) {
        super(name, initialBalance);
        tradeBalance = initialBalance;
    }

    public double runSimulation() {
        initialize();
        for (int tradeCount = 1; tradeCount <= maxTrades; tradeCount++) {
            if ((bankBalance * (1 - taxRate)) > 10000000 || (tradeBalance * (1 - taxRate)) > 10000000) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }
            double riskPerContract = (contractPrice * pointsRisk);
            double numberContracts = Math.floor(tradeBalance * accountRisk / riskPerContract);
//            riskAmount = (tradeBalance * 0.05) % (numberContracts * riskPerContract);
            if (isWinningTrade()) {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleProfitArr.length);
                double rMultiple = rMultipleProfitArr[rMultipleIndex];
                double netGain = numberContracts * rMultiple * contractPrice * pointsRisk;
                tradeBalance += netGain;
                totalWinsAccumulated += netGain;
                if (logResults) {
                    printTrade(tradeCount, netGain, rMultiple, tradeBalance, bankBalance, score, level);
                }
                if (takePercentProfitsPercent > 0) {
                    double percentProfits = netGain * takePercentProfitsPercent;
                    bankBalance += percentProfits;
                    tradeBalance -= percentProfits;
                }
                if (useBankRoll && tradeBalance > profitLevels[level]) {
                    bankBalance += profitLevels[level] / 2;
                    tradeBalance -= profitLevels[level] / 2;
                    if (level < profitLevels.length - 1) {
                        level++;
                    }
                }
            } else {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleLossArr.length);
                double rMultiple = rMultipleLossArr[rMultipleIndex];
                double netLoss = numberContracts * rMultiple * contractPrice * pointsRisk * -1;
                tradeBalance += netLoss;
                totalLossesIncurred += netLoss;
                if (logResults) {
                    printTrade(tradeCount, netLoss, 1, tradeBalance, bankBalance, score, level);
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
            double commissionsPerSide = numberContracts * commission;
            totalCommissionsPaid += commissionsPerSide;
            if (payCommissionsFromTradeBalance) {
                tradeBalance -= (commissionsPerSide * 2);
            } else {
                bankBalance -= (commissionsPerSide * 2);
            }
            if (logResults) {
                sleep(1000);
            }
        }
        double finalBalance = tradeBalance + bankBalance;
        double taxesPaid = finalBalance > 0 ? (finalBalance) * taxRate : 0;
        double finalBalanceAfterTax = finalBalance - taxesPaid;
        if (logResults) {
            System.out.println("===========================================================");
            System.out.println("Final Balance: " + printBalance(finalBalance) + " | After Taxes: " + printBalance(finalBalanceAfterTax) + " | Taxes Paid: " + printBalance(taxesPaid) + " | ROIC: " + calculateROIC(initialBalance, finalBalanceAfterTax));
            System.out.println("Total Wins: " + printBalance(totalWinsAccumulated) + " | Total Losses: " + printBalance(totalLossesIncurred) + " Total Commissions: " + printBalance(totalCommissionsPaid));
            System.out.println("===========================================================");
        }
        return finalBalanceAfterTax;
    }
}
