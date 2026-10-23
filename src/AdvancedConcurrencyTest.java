
import enums.SpotType;
import model.*;
import service.ParkingManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AdvancedConcurrencyTest {

    static class Booking {
        ParkingManager manager;
        ParkingTicket ticket;

        Booking(ParkingManager manager, ParkingTicket ticket) {
            this.manager = manager;
            this.ticket = ticket;
        }
    }

    private static void result(String test, boolean passed) {
        System.out.println((passed ? "PASS: " : "FAIL: ") + test);
    }

    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = ParkingLot.getInstance();

        ParkingFloor floor = new ParkingFloor(1);
        floor.addSpot(new ParkingSpot(1, SpotType.SMALL));
        floor.addSpot(new ParkingSpot(2, SpotType.MEDIUM));
        floor.addSpot(new ParkingSpot(3, SpotType.LARGE));
        floor.addSpot(new ParkingSpot(4, SpotType.MEDIUM));
        floor.addSpot(new ParkingSpot(5, SpotType.LARGE));
        lot.addFloor(floor);

        // Two independent managers share the same parking lot.
        ParkingManager manager1 = new ParkingManager(lot);
        ParkingManager manager2 = new ParkingManager(lot);

        List<Booking> bookings =
                Collections.synchronizedList(new ArrayList<>());

        Set<Integer> assignedSpots = ConcurrentHashMap.newKeySet();

        AtomicInteger parked = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        System.out.println("\n===================================");
        System.out.println("   ADVANCED CONCURRENCY TESTS");
        System.out.println("===================================");

        // TEST 1: Six cars compete through two managers.
        System.out.println("\nTEST 1: Concurrent check-in across managers");

        int totalCars = 6;
        CountDownLatch ready = new CountDownLatch(totalCars);
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < totalCars; i++) {
            final int id = i;
            final ParkingManager manager =
                    (i % 2 == 0) ? manager1 : manager2;

            Thread thread = new Thread(() -> {
                ready.countDown();

                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                Vehicle car = new Car("RACE-" + id);
                ParkingTicket ticket = manager.checkIn(car);

                if (ticket != null) {
                    parked.incrementAndGet();
                    bookings.add(new Booking(manager, ticket));

                    if (!assignedSpots.add(
                            ticket.getParkingSpot().getSpotId())) {
                        System.out.println(
                                "ERROR: Duplicate spot assigned!");
                    }
                } else {
                    rejected.incrementAndGet();
                }
            }, "RACE-" + i);

            threads.add(thread);
            thread.start();
        }

        ready.await();
        start.countDown();

        for (Thread thread : threads) {
            thread.join();
        }

        boolean capacityPassed =
                parked.get() == 4
                        && rejected.get() == 2
                        && assignedSpots.size() == 4;

        result("Six cars: four parked, two rejected, unique spots",
                capacityPassed);

        // TEST 2: Two threads check out the same ticket.
        System.out.println("\nTEST 2: Simultaneous checkout of one ticket");

        Booking target = bookings.get(0);

        CountDownLatch checkoutStart = new CountDownLatch(1);
        AtomicInteger successfulCheckouts = new AtomicInteger();
        AtomicInteger rejectedCheckouts = new AtomicInteger();

        Runnable checkoutTask = () -> {
            try {
                checkoutStart.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            double fee = target.manager.checkOut(
                    target.ticket.getTicketId());

            if (fee > 0) {
                successfulCheckouts.incrementAndGet();
            } else {
                rejectedCheckouts.incrementAndGet();
            }
        };

        Thread checkout1 = new Thread(checkoutTask, "CHECKOUT-1");
        Thread checkout2 = new Thread(checkoutTask, "CHECKOUT-2");

        checkout1.start();
        checkout2.start();

        checkoutStart.countDown();

        checkout1.join();
        checkout2.join();

        boolean duplicateCheckoutPassed =
                successfulCheckouts.get() == 1
                        && rejectedCheckouts.get() == 1;

        result("Exactly one checkout succeeds",
                duplicateCheckoutPassed);

        // TEST 3: Repeated checkout of the same ticket.
        System.out.println("\nTEST 3: Repeated checkout");

        double repeatedFee = target.manager.checkOut(
                target.ticket.getTicketId());

        result("Repeated checkout rejected",
                repeatedFee == 0);

        // TEST 4: Check-in and checkout run concurrently.
        System.out.println("\nTEST 4: Concurrent check-in and checkout");

        Booking checkoutTarget = bookings.get(1);

        CountDownLatch mixedStart = new CountDownLatch(1);

        AtomicReference<ParkingTicket> newTicket =
                new AtomicReference<>();

        AtomicReference<Double> checkoutFee =
                new AtomicReference<>(0.0);

        Thread checkInThread = new Thread(() -> {
            try {
                mixedStart.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            newTicket.set(manager2.checkIn(
                    new Bike("MIXED-NEW")));
        }, "MIXED-CHECKIN");

        Thread checkOutThread = new Thread(() -> {
            try {
                mixedStart.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            checkoutFee.set(checkoutTarget.manager.checkOut(
                    checkoutTarget.ticket.getTicketId()));
        }, "MIXED-CHECKOUT");

        checkInThread.start();
        checkOutThread.start();

        mixedStart.countDown();

        checkInThread.join();
        checkOutThread.join();

        boolean mixedPassed =
                newTicket.get() != null
                        && checkoutFee.get() > 0;

        result("Concurrent check-in and checkout both complete",
                mixedPassed);

        if (newTicket.get() != null) {
            bookings.add(new Booking(manager2, newTicket.get()));
        }

        // TEST 5: All successful tickets should have unique IDs.
        System.out.println("\nTEST 5: Ticket ID uniqueness");

        Set<String> ticketIds = new HashSet<>();
        boolean uniqueTickets = true;

        for (Booking booking : bookings) {
            if (!ticketIds.add(booking.ticket.getTicketId())) {
                uniqueTickets = false;
                break;
            }
        }

        result("All generated ticket IDs are unique", uniqueTickets);

        // TEST 6: Every assigned spot is valid for a car.
        System.out.println("\nTEST 6: Spot compatibility");

        boolean compatibilityPassed = true;

        for (Booking booking : bookings) {
            if (booking.ticket.getVehicle().getVehicleType()
                    == enums.VehicleType.CAR) {

                SpotType type =
                        booking.ticket.getParkingSpot().getSpotType();

                if (type != SpotType.MEDIUM
                        && type != SpotType.LARGE) {
                    compatibilityPassed = false;
                    break;
                }
            }
        }

        result("No car was assigned a small spot",
                compatibilityPassed);

        // TEST 7: Clean up remaining active tickets.
        System.out.println("\nTEST 7: Checkout cleanup");

        for (Booking booking : bookings) {
            booking.manager.checkOut(
                    booking.ticket.getTicketId());
        }

        // Verify that all five spots are available.
        int available = 0;

        for (ParkingFloor parkingFloor : lot.getFloors()) {
            for (ParkingSpot spot : parkingFloor.getSpots()) {
                if (spot.isAvailable()) {
                    available++;
                }
            }
        }

        result("All five spots are available after cleanup",
                available == 5);

        System.out.println("\nAvailable spots: " + available);
        System.out.println("===================================");
        System.out.println("ADVANCED TESTS COMPLETED");
        System.out.println("===================================");
    }
}
