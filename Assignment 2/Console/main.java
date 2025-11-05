import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Queue;



 class Semaphore {
    // Holds the current value of the semaphore
    private int value;

    // Constructor to initialize the semaphore with a given value
    public Semaphore(int Value) {
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
            } catch (InterruptedException e) {}
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
        System.out.println( carID + " arrived");
        empty.P();
        mutex.P();
        waitingQueue.add(this);
        System.out.println( carID + " arrived and waiting");
        mutex.V();
        full.V();
    }
}


 class Pump extends Thread {
    private int pumpID;
    private Queue<Car> waitingQueue;
    private Semaphore mutex;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore pumps;

    public Pump(int id, Queue<Car> queue, Semaphore m, Semaphore e, Semaphore f, Semaphore p) {
        pumpID = id;
        waitingQueue = queue;
        mutex = m;
        empty = e;
        full = f;
        pumps = p;
    }

    @Override
    public void run() {
        while (true) {
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
                return;
            }

            if (car != null) {
                System.out.println("Pump " + pumpID + ": " + car.getCarID() + " finishes service");
                System.out.println("Pump " + pumpID + ": Bay " + pumpID + " is now free");
            }

            pumps.V();
        }
    }
}


 class ServiceStation {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter waiting area capacity (1–10): ");
        int waitingArea = input.nextInt();

        System.out.print("Enter number of service bays (pumps): ");
        int pumpNums = input.nextInt();

        input.nextLine(); // So we can get the next line completely and correctly

        System.out.print("Enter car IDs separated by spaces (e.g. C1 C2 C3 C4 C5): ");
        String[] carIDs = input.nextLine().trim().split("[,\\s]+"); // Regix for Spaces and commas so it can split input by spaces or commas



        input.close();

        Queue<Car> waitingQueue = new LinkedList<>();

        Semaphore mutex = new Semaphore(1);
        Semaphore empty = new Semaphore(waitingArea);
        Semaphore full = new Semaphore(0);
        Semaphore pumps = new Semaphore(pumpNums);

        for (int i = 1; i <= pumpNums; i++) {
            Pump pump = new Pump(i, waitingQueue, mutex, empty, full, pumps);
            pump.start();
        }


        for (String id : carIDs) {
            Car car = new Car(id, waitingQueue, mutex, empty, full);
            car.start();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }
}
