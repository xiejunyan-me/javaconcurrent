package threadlearn;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class BlockedQueue<T> {
    final Lock lock = new ReentrantLock();
    // 条件变量：队列不满
    final Condition notFull = lock.newCondition();
    // 条件变量：队列不空
    final Condition notEmpty = lock.newCondition();

    private LinkedList<T> mList = new LinkedList<>();

    // 入队
    void enq(T x) throws InterruptedException {
        lock.lock();
        try {
            while (mList.size() == 2) {
                // 等待队列不满
                System.out.println("队列满了");
                notFull.await();
            }
            // add x to queue
            // 入队后,通知可出队
            mList.add(0, x);
            System.out.println("插入"+x);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // 出队
    void deq() throws InterruptedException {
        lock.lock();
        try {
            while (mList.size() == 0) {
                // 等待队列不空
                System.out.println("队列空了");
                notEmpty.await();
            }
            // remove the first element from queue
            // 出队后，通知可入队
            var popX = mList.pop();
            System.out.println("弹出"+popX);
            notFull.signal();
        } finally {
            lock.unlock();
        }
    }
}


public class Test1 {
    public static void main(String[] args) throws InterruptedException {
        var queue = new BlockedQueue<Integer>();
        var random = new Random();
        var thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        queue.enq(random.nextInt());
                        Thread.sleep(random.nextInt(2000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        var thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        queue.deq();
                        Thread.sleep(random.nextInt(2000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.start();
        thread2.start();
        Thread.sleep(50000);
    }
}
