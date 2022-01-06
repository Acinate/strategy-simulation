import java.util.concurrent.ThreadLocalRandom;

public class FuturesStrategy extends Strategy {

    double pointsRisk = 4;
    double riskPerTrade = 0.05;
    double[] rMultipleLossArr = {-0.75, -1};
    double[] rMultipleProfitArr = {1.5, 1.75, 2.0};

    public FuturesStrategy(String name, double initialBalance) {
        super(name, initialBalance);
        tradeBalance = initialBalance;
    }

    public double runSimulation() {
        resetBalances();

        if (initialBalance * riskPerTrade < 20) {
            if (logResults) {
                System.out.println("Initial balance not high enough for risk employed.");
            }
            return -1;
        }

        for (int tradeCount = 1; tradeCount <= maxTrades; tradeCount++) {
            if (isMaxBalanceReached()) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }

            boolean useMicroContract = tradeBalance * riskPerTrade < 200;
            double pricePerContract = useMicroContract ? 5 : 50;
            commission = useMicroContract ? 0.22 : 0.79;
            double riskPerContract = (pricePerContract * pointsRisk);
            double numberContracts = Math.floor((tradeBalance * riskPerTrade) / riskPerContract);

            if (numberContracts <= 0) {
                double amtToDeposit = initialBalance - tradeBalance;
                bankBalance -= amtToDeposit;
                tradeBalance += amtToDeposit;
            }

            double netGain;
            double rMultiple;
            if (isWinningTrade()) {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleProfitArr.length);
                rMultiple = rMultipleProfitArr[rMultipleIndex];
                netGain = numberContracts * rMultiple * pricePerContract * pointsRisk;
                totalWinsAccumulated += netGain;
            } else {
                int rMultipleIndex = ThreadLocalRandom.current().nextInt(0, rMultipleLossArr.length);
                rMultiple = rMultipleLossArr[rMultipleIndex];
                netGain = numberContracts * rMultiple * pricePerContract * pointsRisk;
                totalLossesIncurred += netGain;
            }
            tradeBalance += netGain;

            double commissionsPerSide = numberContracts * commission;
            double commissionsPaid = commissionsPerSide * 2;
            tradeBalance -= commissionsPaid;
            totalCommissionsPaid += commissionsPaid;

            if (logResults) {
                printTrade(tradeCount, netGain, rMultiple, tradeBalance, numberContracts);
                sleep(1000);
            }
        }
        double finalBalance = tradeBalance + bankBalance;
        double taxesPaid = finalBalance > 0 ? (finalBalance) * 0.25 : 0;
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
