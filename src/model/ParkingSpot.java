
package model;

import enums.SpotType;

public class ParkingSpot {

    private int spotId;
    private SpotType spotType;
    private boolean occupied;

    public ParkingSpot(int spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.occupied = false;
    }

    public int getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public boolean isAvailable() {
        return !occupied;
    }

    public void occupy() {
        occupied = true;
    }

    public void free() {
        occupied = false;
    }
}
