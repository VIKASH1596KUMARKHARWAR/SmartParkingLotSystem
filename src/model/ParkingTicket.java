
package model;

import java.time.LocalDateTime;

public class ParkingTicket {

    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot parkingSpot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    public ParkingTicket(String ticketId, Vehicle vehicle,
                         ParkingSpot parkingSpot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryTime = LocalDateTime.now();
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void closeTicket() {
        this.exitTime = LocalDateTime.now();
    }
}
