
package strategy;

public class BusFeeStrategy implements FeeStrategy {

    @Override
    public double calculateFee(long hours) {
        return hours * 100;
    }
}
