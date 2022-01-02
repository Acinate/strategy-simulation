import java.util.concurrent.ThreadLocalRandom;

public class FuturesStrategy extends Strategy {

    double pointsPrice = 5;
    double pointsRisk = 4;
    double accountRisk = 0.05;
    double[] rMultipleLossArr = {1};
    double[] rMultipleProfitArr = {1.5, 1.75, 2.0};
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
            double riskPerContract = (pointsPrice * pointsRisk);
            double numberContracts = Math.floor(tradeBalance * accountRisk / riskPerContract);
            if (numberContracts <= 0) {
                double amtToDeposit = initialBalance - tradeBalance;
                bankBalance -= amtToDeposit;
                tradeBalance += amtToDeposit;
                if (level > 1) {
                    level--;
                }
            }

            double netGain;
            if (isWinningTrade()) {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleProfitArr.length);
                double rMultiple = rMultipleProfitArr[rMultipleIndex];
                netGain = numberContracts * rMultiple * pointsPrice * pointsRisk;
            } else {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleLossArr.length);
                double rMultiple = rMultipleLossArr[rMultipleIndex];
                netGain = -1 * numberContracts * rMultiple * pointsPrice * pointsRisk;
            }
            tradeBalance += netGain;
            totalLossesIncurred += netGain;

            double commissionsPerSide = numberContracts >= 10 ? numberContracts / 10 * commission : numberContracts * commission;
            totalCommissionsPaid += commissionsPerSide;
            if (payCommissionsFromTradeBalance) {
                tradeBalance -= (commissionsPerSide * 2);
            } else {
                bankBalance -= (commissionsPerSide * 2);
            }

            if (logResults) {
                printTrade(tradeCount, netGain, 1, tradeBalance, bankBalance, score, level);
                sleep(1000);
            }
        }
        double finalBalance = tradeBalance + bankBalance;
        double taxesPaid = finalBalance > 0 ? (finalBalance) * 0.20 : 0;
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
