import java.util.concurrent.atomic.AtomicReference;

public class CohortLockTest {
    private static final long NUM_OPERATIONS = 100_000_000L;

    public static void main(String[] args) {
        int[] threadCounts = {1, 2, 4, 6, 8, 10, 12, 14, 16};

        for (int numThreads : threadCounts) {
            long totalTime = 0;
            double totalThroughput = 0;


            for (int i = 0; i < 5; i++) {
                long elapsedTime = runTest(numThreads, NUM_OPERATIONS);
                double throughput = (double) NUM_OPERATIONS / elapsedTime * 1000; // Operations per second

                totalTime += elapsedTime;
                totalThroughput += throughput;
                
            }

            long avgTime = totalTime / 5;
            double avgThroughput = totalThroughput / 5;
            
            System.out.println("Threads: " + numThreads);
            System.out.println("Fuck me");
            System.out.println("Average Time taken: " + avgTime + " ms");
            System.out.println("Average Throughput: " + avgThroughput + " ops/s");
            System.out.println();
        }
    }

    private static long runTest(int numThreads, long numOperations) {
        CohortLock lock = new CohortLock();
        LazyList list = new LazyList();
        AtomicReference<Integer> sharedCounter = new AtomicReference<>(0);

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (long j = 0; j < numOperations / numThreads; j++) {
                    performOperation(lock, list, sharedCounter);
                }
            });
            threads[i].start();
        }

        long startTime = System.currentTimeMillis();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static void performOperation(CohortLock lock, LazyList list, AtomicReference<Integer> sharedCounter) {
        int operation = (int) (Math.random() * 100);
        int item = (int) (Math.random() * 1000); // For testing, generate a random item

        if (operation < 50) { // Compute
            computeOperation(lock, sharedCounter);
        } else if (operation < 75) { // Insert
            list.insert(item); // Insert random item into LazyList
        } else { // Delete
            list.remove(item); // Remove random item from LazyList
        }
    }

    private static void computeOperation(CohortLock lock, AtomicReference<Integer> sharedCounter) {
        lock.lock();
        try {
            sharedCounter.set(sharedCounter.get() + 1);
        } finally {
            lock.unlock();
        }
    }
}
