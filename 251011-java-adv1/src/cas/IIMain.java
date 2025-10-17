package cas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static control.ThreadUtils.sleep;

public class IIMain {
    public static final int THREAD_COUNT = 10000;
    public static final int LOOP_COUNT = 10;
    public static void main(String[] args) throws InterruptedException {
        test(new II1());
        test(new II2());
        test(new II3());
        test(new II4());
    }
    private static void test(IncrementInteger ii) throws InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sleep(10);
                ii.increment();
            }
        };
        int[] seconds = new int[LOOP_COUNT];
        for (int i = 0; i < LOOP_COUNT; i++) {
            long startMs = System.currentTimeMillis();
            List<Thread> threads = new ArrayList<>();
            for (int j = 0; j < THREAD_COUNT; j++) {
                Thread t = new Thread(runnable);
                threads.add(t);
                t.start(); //1 증가 * 100번
            }
            for (Thread t : threads) {
                t.join(); //throws InterruptedException
            }
            long endMs = System.currentTimeMillis();
            seconds[i] = (int) (endMs - startMs);
        }
        System.out.print(ii.getClass().getSimpleName() + ": result=" + ii.get() / LOOP_COUNT);
        Arrays.sort(seconds);
        System.out.println(", duration=" + Arrays.toString(seconds));
    }
}
/*
II1: result=986974, duration=[1186, 1229, 1302, 1545, 1811, 1891, 1982, 2201, 2481, 2928]
II2: result=937032, duration=[1156, 1307, 1811, 1959, 2056, 2475, 2480, 2752, 2903, 3453]
II3: result=1000000, duration=[955, 1410, 2082, 2563, 2732, 2932, 3123, 3308, 3378, 3392]
II4: result=1000000, duration=[2551, 3434, 3577, 3645, 3701, 3729, 3796, 3843, 4101, 4410]
 */