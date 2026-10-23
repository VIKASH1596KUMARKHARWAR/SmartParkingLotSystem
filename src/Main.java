
import enums.SpotType;
import model.*;
import service.ParkingManager;

public class Main {

    private static void printCheckIn(
            Vehicle vehicle, ParkingTicket ticket) {

        System.out.println("\n===================================");
        System.out.println("           VEHICLE CHECK-IN");
        System.out.println("===================================");
        System.out.println("Vehicle Number : " + vehicle.getVehicleNumber());
        System.out.println("Vehicle Type   : " + vehicle.getVehicleType());

        if (ticket != null) {
            System.out.println("Check-In Status: SUCCESS");
            System.out.println("Ticket ID      : " + ticket.getTicketId());
            System.out.println("Spot ID        : "
                    + ticket.getParkingSpot().getSpotId());
            System.out.println("Entry Time     : " + ticket.getEntryTime());
        } else {
            System.out.println("Check-In Status: REJECTED");
            System.out.println("Reason         : No compatible parking spot available");
            System.out.println("Ticket ID      : Not generated");
        }

        System.out.println("===================================");
    }

    private static void printCheckOut(
            ParkingManager manager, ParkingTicket ticket) {

        if (ticket == null) {
            return;
        }

        System.out.println("\n===================================");
        System.out.println("           VEHICLE CHECK-OUT");
        System.out.println("===================================");
        System.out.println("Vehicle Number : "
                + ticket.getVehicle().getVehicleNumber());
        System.out.println("Vehicle Type   : "
                + ticket.getVehicle().getVehicleType());
        System.out.println("Ticket ID      : " + ticket.getTicketId());
        System.out.println("Entry Time     : " + ticket.getEntryTime());

        double fee = manager.checkOut(ticket.getTicketId());

        System.out.println("Exit Time      : " + ticket.getExitTime());
        System.out.println("Parking Fee    : Rs. " + fee);
        System.out.println("Check-Out Status: SUCCESS");
        System.out.println("===================================");
    }

    public static void main(String[] args) {

        ParkingLot parkingLot = ParkingLot.getInstance();

        ParkingFloor floor1 = new ParkingFloor(1);

        floor1.addSpot(new ParkingSpot(1, SpotType.SMALL));
        floor1.addSpot(new ParkingSpot(2, SpotType.MEDIUM));
        floor1.addSpot(new ParkingSpot(3, SpotType.LARGE));

        parkingLot.addFloor(floor1);

        ParkingManager manager = new ParkingManager(parkingLot);

        System.out.println("\n===================================");
        System.out.println("      SMART PARKING LOT SYSTEM");
        System.out.println("===================================");

        System.out.println("\n===== INITIAL PARKING STATUS =====");
        manager.displayAvailableSpots();

        // 1. Bike check-in
        Vehicle bike = new Bike("UP16BK1234");
        ParkingTicket bikeTicket = manager.checkIn(bike);
        printCheckIn(bike, bikeTicket);
        manager.displayAvailableSpots();

        // 2. Car check-in
        Vehicle car = new Car("UP16CR5678");
        ParkingTicket carTicket = manager.checkIn(car);
        printCheckIn(car, carTicket);
        manager.displayAvailableSpots();

        // 3. Bus check-in
        Vehicle bus = new Bus("UP16BS9999");
        ParkingTicket busTicket = manager.checkIn(bus);
        printCheckIn(bus, busTicket);
        manager.displayAvailableSpots();

        // 4. Attempt to park another bus
        Vehicle bus2 = new Bus("UP16BS1111");
        ParkingTicket busTicket2 = manager.checkIn(bus2);
        printCheckIn(bus2, busTicket2);
        manager.displayAvailableSpots();

        // 5. Invalid ticket test
        System.out.println("\n===================================");
        System.out.println("       INVALID CHECK-OUT TEST");
        System.out.println("===================================");

        double invalidFee = manager.checkOut("INVALID-TICKET-ID");

        System.out.println("Ticket ID       : INVALID-TICKET-ID");
        System.out.println("Check-Out Status: "
                + (invalidFee == 0 ? "REJECTED" : "UNEXPECTED"));
        System.out.println("Returned Fee    : Rs. " + invalidFee);

        // 6. Check out successfully parked vehicles
        printCheckOut(manager, bikeTicket);
        manager.displayAvailableSpots();

        printCheckOut(manager, carTicket);
        manager.displayAvailableSpots();

        printCheckOut(manager, busTicket);
        manager.displayAvailableSpots();

        // 7. Reuse a freed spot
        System.out.println("\n===== REUSE FREED SPOT TEST =====");

        Vehicle newBike = new Bike("UP16NEW123");
        ParkingTicket newBikeTicket = manager.checkIn(newBike);
        printCheckIn(newBike, newBikeTicket);
        manager.displayAvailableSpots();

        System.out.println("\n===================================");
        System.out.println("          TESTS COMPLETED");
        System.out.println("===================================");
    }
}
