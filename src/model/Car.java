
package model;

import enums.VehicleType;

public class Car extends Vehicle {

    public Car(String vehicleNumber) {
        super(vehicleNumber, VehicleType.CAR);
    }
}
