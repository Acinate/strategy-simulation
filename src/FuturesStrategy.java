import java.util.concurrent.ThreadLocalRandom;

public class FuturesStrategy extends Strategy {

    double pointsRisk = 4;
    double accountRisk = 0.05;
    double[] rMultipleLossArr = {-0.75, -1};
    double[] rMultipleProfitArr = {1.5, 1.75, 2.0};
    double commission = 0;

    public FuturesStrategy(String name, double initialBalance) {
        super(name, initialBalance);
        tradeBalance = initialBalance;
    }

    public double runSimulation() {
        initialize();

        if (initialBalance * accountRisk < 20) {
            if (logResults) {
                System.out.println("Initial balance not high enough for risk employed.");
            }
            return -1;
        }

        for (int tradeCount = 1; tradeCount <= maxTrades; tradeCount++) {
            if ((bankBalance * (1 - taxRate)) > 10000000 || (tradeBalance * (1 - taxRate)) > 10000000) {
                if (logResults) {
                    System.out.println("Terminated after " + tradeCount + " trades.");
                }
                break;
            }

            boolean useMicroContract = tradeBalance * accountRisk < 200;
            double pricePerContract = useMicroContract ? 5 : 50;
            commission = useMicroContract ? 0.22 : 0.79;
            double riskPerContract = (pricePerContract * pointsRisk);
            double numberContracts = Math.floor((tradeBalance * accountRisk) / riskPerContract);

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
            totalCommissionsPaid += commissionsPaid;
            tradeBalance -= commissionsPaid;

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

    void printTrade(int tradeCount, double netGain, double rValue, double tradeBalance, double numberContracts) {
        String tradeCountStr = "[" + tradeCount + "] " + (netGain > 0 ? ANSI_GREEN + "W" + ANSI_RESET : ANSI_RED + "L" + ANSI_RESET);
        String tradeGainStr = "Trade Gain: " + printBalance(netGain) + " " + printRatio(rValue);
        String accountBalanceStr = "Account Balance: " + printBalance(tradeBalance, (tradeBalance - initialBalance) / initialBalance);
        String numberContractsStr = "# Contracts: " + (int) Math.floor(numberContracts);
        String commissionsPaidStr = "Commissions paid: " + printBalance(-1 * numberContracts * commission * 2);
        System.out.format("%8s%3s%24s%3s%28s%3s%14s%3s%24s%1s", tradeCountStr, " | ", tradeGainStr, " | ", accountBalanceStr, " | ", numberContractsStr, " | ", commissionsPaidStr, "\n");
    }
}
