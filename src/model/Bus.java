
package model;

import enums.VehicleType;

public class Bus extends Vehicle {

    public Bus(String vehicleNumber) {
        super(vehicleNumber, VehicleType.BUS);
    }
}
