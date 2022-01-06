import java.util.concurrent.ThreadLocalRandom;

public class CryptoStrategy extends Strategy {

    double riskPerTrade = 0.05;
    double[] rMultipleLossArr = {-0.75, -1};
    double[] rMultipleProfitArr = {1.5, 1.75, 2.0};

    public CryptoStrategy(String name, double initialBalance) {
        super(name, initialBalance);
    }

    public double runSimulation() {
        resetBalances();

        for (int tradeCount = 1; tradeCount <= maxTrades; tradeCount++) {
            if (isMaxBalanceReached()) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }

            if (tradeBalance < bankruptBalance) {
                double amtToDeposit = initialBalance - tradeBalance;
                bankBalance -= amtToDeposit;
                tradeBalance += amtToDeposit;
                if (level > 1) {
                    level--;
                }
            }

            double netGain;
            double rMultiple;
            if (isWinningTrade()) {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleProfitArr.length);
                rMultiple = rMultipleProfitArr[rMultipleIndex];
                netGain = tradeBalance * riskPerTrade * rMultiple;
                tradeBalance += netGain;
                totalWinsAccumulated += netGain;
                if (logResults) {
                    printTrade(tradeCount, netGain, rMultiple, tradeBalance);
                }
            } else {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleLossArr.length);
                rMultiple = rMultipleLossArr[rMultipleIndex];
                netGain = tradeBalance * riskPerTrade * rMultiple;
                tradeBalance += netGain;
                totalLossesIncurred += netGain;
                if (logResults) {
                    printTrade(tradeCount, netGain, rMultiple, tradeBalance);
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
