import java.util.concurrent.atomic.AtomicReference;

public class CohortLock {
    private final ThreadLocal<CLHNode> myPred;
    private final ThreadLocal<CLHNode> myNode;
    private final AtomicReference<CLHNode> tail;

    public CohortLock() {
        tail = new AtomicReference<>(new CLHNode());
        myNode = ThreadLocal.withInitial(CLHNode::new);
        myPred = ThreadLocal.withInitial(() -> null);
    }

    public void lock() {
        CLHNode qnode = myNode.get();
        qnode.locked = true;
        CLHNode pred = tail.getAndSet(qnode);
        myPred.set(pred);

        // Small delay to reduce CPU usage during waiting
        while (pred.locked) {
            Thread.yield(); // Add a small delay
        }
    }

    public void unlock() {
        CLHNode qnode = myNode.get();
        qnode.locked = false;
        myNode.set(myPred.get());
    }

    private static class CLHNode {
        volatile boolean locked = false;
    }
}
