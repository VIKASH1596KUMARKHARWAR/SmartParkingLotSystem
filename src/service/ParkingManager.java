
package service;

import enums.SpotType;
import enums.VehicleType;
import model.*;
import strategy.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParkingManager {

    private ParkingLot parkingLot;
    private Map<String, ParkingTicket> activeTickets;

    public ParkingManager(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        this.activeTickets = new HashMap<>();
    }


    public synchronized ParkingTicket checkIn(Vehicle vehicle) {

        synchronized (parkingLot) {

            ParkingSpot spot = allocateSpot(vehicle);

            if (spot == null) {
                return null;
            }

            spot.occupy();

            String ticketId = UUID.randomUUID().toString();

            ParkingTicket ticket =
                    new ParkingTicket(ticketId, vehicle, spot);

            activeTickets.put(ticketId, ticket);

            return ticket;
        }
    }

    public synchronized double checkOut(String ticketId) {

        ParkingTicket ticket = activeTickets.get(ticketId);

        if (ticket == null) {
            return 0;
        }

        synchronized (parkingLot) {

            // Recheck after acquiring the shared lock.
            ticket = activeTickets.get(ticketId);

            if (ticket == null) {
                return 0;
            }

            ticket.closeTicket();

            long hours = Duration.between(
                    ticket.getEntryTime(),
                    ticket.getExitTime()
            ).toHours();

            if (hours == 0) {
                hours = 1;
            }

            FeeStrategy strategy =
                    getFeeStrategy(ticket.getVehicle().getVehicleType());

            double fee = strategy.calculateFee(hours);

            ticket.getParkingSpot().free();

            activeTickets.remove(ticketId);

            return fee;
        }
    }

    private ParkingSpot allocateSpot(Vehicle vehicle) {

        for (ParkingFloor floor : parkingLot.getFloors()) {

            for (ParkingSpot spot : floor.getSpots()) {

                if (spot.isAvailable() &&
                        canFit(vehicle.getVehicleType(),
                                spot.getSpotType())) {

                    return spot;
                }
            }
        }

        return null;
    }

    private boolean canFit(VehicleType vehicleType,
                           SpotType spotType) {

        switch (vehicleType) {

            case BIKE:
                return true;

            case CAR:
                return spotType == SpotType.MEDIUM
                        || spotType == SpotType.LARGE;

            case BUS:
                return spotType == SpotType.LARGE;

            default:
                return false;
        }
    }

    private FeeStrategy getFeeStrategy(VehicleType type) {

        switch (type) {

            case BIKE:
                return new BikeFeeStrategy();

            case CAR:
                return new CarFeeStrategy();

            case BUS:
                return new BusFeeStrategy();

            default:
                throw new IllegalArgumentException();
        }
    }

    public void displayAvailableSpots() {

        int availableSpots = 0;

        for (ParkingFloor floor : parkingLot.getFloors()) {

            for (ParkingSpot spot : floor.getSpots()) {

                if (spot.isAvailable()) {
                    availableSpots++;
                }
            }
        }

        System.out.println("Available Spots : " + availableSpots);
    }
}
