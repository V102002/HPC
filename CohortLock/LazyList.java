import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class LazyList {
    private Node head;
    private Node tail;
    private Lock lock = new ReentrantLock();

    public LazyList() {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
    }

    private static class Node {
        private int key;
        private Node next;
        private Lock lock;
        private boolean marked;

        Node(int item) {
            this.key = item;
            this.next = null;
            this.lock = new ReentrantLock();
            this.marked = false;
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }

    private boolean validate(Node pred, Node curr) {
        synchronized (pred) {
            synchronized (curr) {
                return !pred.marked && !curr.marked && pred.next == curr;
            }
        }
    }

    public boolean remove(int item) {
        Node pred, curr;
        int key = item;
        while (true) {
            pred = head;
            curr = head.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key == key) {
                            curr.marked = true;
                            pred.next = curr.next;
                            return true;
                        } else {
                            return false;
                        }
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    public void insert(int item) {
        Node newNode = new Node(item);
        lock.lock();
        try {
            Node pred = head;
            Node curr = head.next;
            while (curr.key < item) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        newNode.next = curr;
                        pred.next = newNode;
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        } finally {
            lock.unlock();
        }
    }

    // Other methods...
}
