
package strategy;

public class BikeFeeStrategy implements FeeStrategy {

    @Override
    public double calculateFee(long hours) {
        return hours * 20;
    }
}
