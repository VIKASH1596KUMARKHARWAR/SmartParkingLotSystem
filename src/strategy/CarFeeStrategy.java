
package strategy;

public class CarFeeStrategy implements FeeStrategy {

    @Override
    public double calculateFee(long hours) {
        return hours * 50;
    }
}
