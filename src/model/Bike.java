
package model;

import enums.VehicleType;

public class Bike extends Vehicle {

    public Bike(String vehicleNumber) {
        super(vehicleNumber, VehicleType.BIKE);
    }
}
