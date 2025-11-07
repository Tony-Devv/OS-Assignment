import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;



 class Semaphore {
    // Holds the current value of the semaphore
    private int value;

    // Constructor to initialize the semaphore with a given value
    public Semaphore(int Value) {

        // cannot initialize the Semaphore with negative values
        if(Value < 0){
            throw new IllegalArgumentException("Semaphore value cannot be negative");
        }
        value = Value;
    }

    // P() operation to acquire a resource
    public synchronized void P(){
        // Decrement the semaphore value
        value--;
        // If the semaphore value is less than 0, wait for it to become available
        if(value < 0){
            try {
                // Put the thread to sleep until the semaphore is available
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // V() operation to release a resource
    public synchronized void V(){
        // Increment the semaphore value because a resource is released
        value++;
        // If the semaphore value is less than or equal to 0, notify a waiting thread
        if (value <= 0){
            notify();
        }
    }

}

class Car extends Thread {
    private String carID;
    private Queue<Car> waitingQueue;
    private Semaphore mutex;
    private Semaphore empty;
    private Semaphore full;

    public Car(String id, Queue<Car> queue, Semaphore m, Semaphore e, Semaphore f) {
        if(id == null || id.isEmpty()){
            throw new IllegalArgumentException("Car ID cannot be null or empty");
        }
        carID = id;
        waitingQueue = queue;
        mutex = m;
        empty = e;
        full = f;
    }

    public String getCarID() {
        return carID;
    }

    @Override
    public void run() {
        try{
            System.out.println( carID + " arrived");
            empty.P();
            mutex.P();
            waitingQueue.add(this);
            System.out.println( carID + " arrived and waiting");
            mutex.V();
            full.V();
        }catch(Exception e){
            Thread.currentThread().interrupt();
            System.out.println("Error while processing car " + carID + ": " + e.getMessage());
        }

    }
}


 class Pump extends Thread {
    private int pumpID;
    private Queue<Car> waitingQueue;
    private Semaphore mutex;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore pumps;
    private static int completedCars = 0;
    private static int totalCars;

     public Pump(int id, Queue<Car> queue, Semaphore m, Semaphore e, Semaphore f, Semaphore p) {
        if(id < 1)
        {
            throw new IllegalArgumentException("Pump ID must be positive");
        }
        pumpID = id;
        waitingQueue = queue;
        mutex = m;
        empty = e;
        full = f;
        pumps = p;
    }

     public static void setTotalCars(int count) {
         totalCars = count;
     }

    @Override
    public void run() {
        while (true) {
            try {
                full.P();
                pumps.P();
                mutex.P();

                Car car = waitingQueue.poll();
                if (car != null) {
                    System.out.println("Pump " + pumpID + ": " + car.getCarID() + " login");
                    System.out.println("Pump " + pumpID + ": " + car.getCarID() + " begins service at Bay " + pumpID);
                }
                mutex.V();
                empty.V();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Pump " + pumpID + " interrupted during service.");
                    return;
                }

                if (car != null) {
                    System.out.println("Pump " + pumpID + ": " + car.getCarID() + " finishes service");
                    System.out.println("Pump " + pumpID + ": Bay " + pumpID + " is now free");
                }
                pumps.V();

                synchronized (Pump.class) {    // lock on the entire class not only the object
                    completedCars++;
                    if (completedCars >= totalCars) { // to close the pump after it finishes
                        full.V();
                        break;
                    }
                }
            }catch(Exception e){
                break;
            }
        }
    }
}


 class ServiceStation {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        List<Thread> pumpThreads = new ArrayList<>();
        while(true)
        {
            try{
                System.out.print("Enter waiting area capacity (1–10): ");
                int waitingArea = input.nextInt();
                if(waitingArea > 10 || waitingArea < 1){
                    System.out.println("Invalid waiting area capacity, Must be between 1 and 10");
                    continue;
                }

                System.out.print("Enter number of service bays (pumps): ");
                int pumpNums = input.nextInt();
                if (pumpNums < 1) {
                    System.out.println("There must be at least one pump.");
                    continue;
                }

                input.nextLine(); // So we can get the next line completely and correctly

                System.out.print("Enter car IDs separated by spaces (e.g. C1 C2 C3 C4 C5): ");
                String[] carIDs = input.nextLine().trim().split("[,\\s]+"); // Regix for Spaces and commas so it can split input by spaces or commas
                if(carIDs.length == 0){
                    System.out.println("You must enter at least one car ID");
                    continue;
                }
                Pump.setTotalCars(carIDs.length);

                input.close();

                Queue<Car> waitingQueue = new LinkedList<>();

                Semaphore mutex = new Semaphore(1);
                Semaphore empty = new Semaphore(waitingArea);
                Semaphore full = new Semaphore(0);
                Semaphore pumps = new Semaphore(pumpNums);

                for (int i = 1; i <= pumpNums; i++) {
                    Pump pump = new Pump(i, waitingQueue, mutex, empty, full, pumps);
                    pump.start();
                    pumpThreads.add(pump);
                }

                for (String id : carIDs) {
                    Car car = new Car(id, waitingQueue, mutex, empty, full);
                    car.start();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }

                // Wait for all pumps to finish
                for (Thread pumpThread : pumpThreads) {
                    pumpThread.join();
                }

                System.out.println("All cars processed; simulation ends.");
                break;

            } catch (Exception e) {
                System.out.println("An error occurred during setup: " + e.getMessage());
            }
        }
    }
}
