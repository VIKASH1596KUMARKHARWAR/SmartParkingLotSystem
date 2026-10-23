
import enums.SpotType;
import model.*;
import service.ParkingManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceConditionTest {

    public static void main(String[] args) {

        ParkingLot parkingLot = ParkingLot.getInstance();

        ParkingFloor floor1 = new ParkingFloor(1);

        // Five physical spots, but only four fit cars.
        floor1.addSpot(new ParkingSpot(1, SpotType.SMALL));
        floor1.addSpot(new ParkingSpot(2, SpotType.MEDIUM));
        floor1.addSpot(new ParkingSpot(3, SpotType.LARGE));
        floor1.addSpot(new ParkingSpot(4, SpotType.MEDIUM));
        floor1.addSpot(new ParkingSpot(5, SpotType.LARGE));

        parkingLot.addFloor(floor1);

        ParkingManager manager = new ParkingManager(parkingLot);

        int totalCars = 6;

        AtomicInteger parkedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        Set<Integer> allocatedSpots =
                ConcurrentHashMap.newKeySet();

        AtomicBoolean duplicateSpotDetected =
                new AtomicBoolean(false);

        Object printLock = new Object();

        Runnable task = () -> {

            String vehicleNumber =
                    Thread.currentThread().getName();

            Vehicle car = new Car(vehicleNumber);

            ParkingTicket ticket = manager.checkIn(car);

            synchronized (printLock) {

                System.out.println("\n-----------------------------------");
                System.out.println("       CONCURRENT CHECK-IN");
                System.out.println("-----------------------------------");
                System.out.println("Vehicle Number : " + vehicleNumber);
                System.out.println("Vehicle Type   : " + car.getVehicleType());

                if (ticket != null) {

                    int spotId =
                            ticket.getParkingSpot().getSpotId();

                    parkedCount.incrementAndGet();

                    System.out.println("Status         : PARKED SUCCESSFULLY");
                    System.out.println("Ticket ID      : " + ticket.getTicketId());
                    System.out.println("Spot ID        : " + spotId);
                    System.out.println("Entry Time     : " + ticket.getEntryTime());

                    if (!allocatedSpots.add(spotId)) {
                        duplicateSpotDetected.set(true);
                        System.out.println("ERROR: DUPLICATE SPOT ALLOCATED!");
                    }

                } else {

                    rejectedCount.incrementAndGet();

                    System.out.println("Status         : REJECTED");
                    System.out.println("Reason         : No compatible spot available");
                    System.out.println("Ticket ID      : Not generated");
                }

                System.out.println("-----------------------------------");
            }
        };

        Thread[] threads = new Thread[totalCars];

        System.out.println("\n===================================");
        System.out.println("   PARKING RACE CONDITION TEST");
        System.out.println("===================================");
        System.out.println("Cars competing : " + totalCars);
        System.out.println("Physical spots : 5");
        System.out.println("Compatible car spots : 4");
        System.out.println("\n===== STARTING CONCURRENT CHECK-IN =====");

        for (int i = 0; i < totalCars; i++) {
            threads[i] = new Thread(task, "CAR-" + (i + 1));
        }

        // Start all threads together to create contention.
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Test interrupted.");
            return;
        }

        System.out.println("\n===================================");
        System.out.println("       RACE CONDITION SUMMARY");
        System.out.println("===================================");
        System.out.println("Total cars attempted : " + totalCars);
        System.out.println("Cars parked          : " + parkedCount.get());
        System.out.println("Cars rejected        : " + rejectedCount.get());
        System.out.println("Unique spots assigned: " + allocatedSpots.size());

        System.out.println("\n===== FINAL PARKING STATUS =====");
        manager.displayAvailableSpots();

        boolean passed =
                parkedCount.get() == 4
                        && rejectedCount.get() == 2
                        && allocatedSpots.size() == parkedCount.get()
                        && !duplicateSpotDetected.get();

        System.out.println("\n===================================");
        System.out.println("          TEST RESULT");
        System.out.println("===================================");

        if (passed) {
            System.out.println("PASS: No duplicate spot allocation.");
            System.out.println("PASS: Only compatible spots were assigned.");
            System.out.println("PASS: Excess cars were rejected.");
            System.out.println("PASS: All threads completed.");
            System.out.println("OVERALL RESULT: PASSED");
        } else {
            System.out.println("OVERALL RESULT: FAILED");
            System.out.println("Check spot allocation and thread safety.");
        }
    }
}
