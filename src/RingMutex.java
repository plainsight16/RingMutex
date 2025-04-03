import java.util.concurrent.LinkedBlockingQueue;

class Process implements Runnable {
    private int id;
    private Process nxtProcess;
    private boolean hasToken;
    private final LinkedBlockingQueue<Boolean> requestQueue = new LinkedBlockingQueue<>();

    public Process(int id, boolean hasToken) {
        this.id = id;
        this.hasToken = hasToken;
    }

    public void setNextProcess(Process nxtProcess) {
        this.nxtProcess = nxtProcess;
    }

    public void requestCS() {
        requestQueue.add(true); // Real CS request
    }

    private void enterCriticalSection() {
        System.out.println("Process " + id + " entering critical section.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Process " + id + " has exited critical section.");
    }

    private void passToken() {
        hasToken = false;
        nxtProcess.receiveToken(); // Unconditionally pass the token
        System.out.println("Process " + id + " passed token to Process " + nxtProcess.id);
    }

    private void receiveToken() {
        hasToken = true;
        requestQueue.add(false); // Dummy entry to trigger token processing
    }

    @Override
    public void run() {
        while (true) {
            try {
                Boolean isRealRequest = requestQueue.take();
                if (isRealRequest) {
                    // Handle real CS request
                    if (hasToken) {
                        enterCriticalSection();
                        passToken();
                    } else {
                        System.out.println("Process " + id + " is waiting for token.");
                        requestQueue.add(true); // Requeue the request
                    }
                } else {
                    // Handle token receipt
                    if (hasToken) {
                        if (!requestQueue.isEmpty() && requestQueue.peek()) {
                            // Process the CS request if present
                            enterCriticalSection();
                            passToken();
                        } else {
                            // No requests, pass token immediately
                            passToken();
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

public class RingMutex {
    public static void main(String[] args) {
        int numProcesses = 3;
        Process[] processes = new Process[numProcesses];

        // Initialize processes; initial token given to Process 0
        for (int i = 0; i < numProcesses; i++) {
            processes[i] = new Process(i, i == 0);
        }

        // Set up ring structure
        for (int i = 0; i < numProcesses; i++) {
            processes[i].setNextProcess(processes[(i + 1) % numProcesses]);
        }

        // Start all processes
        for (Process process : processes) {
            new Thread(process).start();
        }

        // Simulate CS requests after delays
        try {
            Thread.sleep(1000);
            processes[0].requestCS(); // Process 0 requests at 1s
            Thread.sleep(3000);
            processes[1].requestCS(); // Process 1 requests at 4s
            Thread.sleep(3000);
            processes[2].requestCS(); // Process 2 requests at 7s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
