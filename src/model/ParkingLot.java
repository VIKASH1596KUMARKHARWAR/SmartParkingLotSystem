
package model;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {

    private static ParkingLot instance;

    private List<ParkingFloor> floors;

    private ParkingLot() {
        floors = new ArrayList<>();
    }

    public static ParkingLot getInstance() {

        if (instance == null) {
            instance = new ParkingLot();
        }

        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }
}
