package sync2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static control.ThreadUtils.sleep;

public class RLMain {
    public final Lock nonFairLock = new ReentrantLock();
    public final Lock fairLock = new ReentrantLock(true);
    public List<String> nonFairResult = Collections.synchronizedList(new ArrayList<>());
    public List<String> fairResult = Collections.synchronizedList(new ArrayList<>());
    public void nonFairLockTest() {
        nonFairLock.lock();
        try {
            Thread.sleep(2); //락을 유지
            nonFairResult.add(Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            nonFairLock.unlock();
        }
    }
    public void fairLockTest() {
        fairLock.lock();
        try {
            Thread.sleep(2); //락을 유지
            fairResult.add(Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            fairLock.unlock();
        }
    }
    public static void main(String[] args) {
        RLMain rlMain = new RLMain();
        int size = 2000;
        System.out.println("비공정");
        runTest(() -> rlMain.nonFairLockTest(), size);
        sleep(10000);
        analyzeResult(rlMain.nonFairResult, size);
        System.out.println();
        System.out.println("공정");
        runTest(() -> rlMain.fairLockTest(), size);
        sleep(10000);
        analyzeResult(rlMain.fairResult, size);
    }
    private static void runTest(Runnable lockTest, int size) {
        for (int i = 0; i < size / 2; i++) {
            Thread t = new Thread(lockTest, Integer.toString(i));
            t.start();
        }
        for (int i = size / 2; i < size; i++) {
            Thread t = new Thread(lockTest, Integer.toString(i));
            t.start();
        }
    }
    private static void analyzeResult(List<String> result, int size) {
        if (result.size() != size) {
            System.out.println("경고: 예상 크기(" + size + ")와 실제 크기(" + result.size() + ")가 다릅니다.");
            return;
        }
        int halfSize = size / 2;
        //여러 체크포인트에서 확인 (전체 크기의 1%, 5%, 10%, 20%, 50%)
        int[] checkRatios = {1, 5, 10, 20, 50};
        System.out.println("=== 섞임 분석 ===");
        for (int ratio : checkRatios) {
            int checkpoint = size * ratio / 100;
            if (checkpoint == 0) continue;
            int secondGroupCount = 0;
            for (int i = 0; i < checkpoint; i++) {
                int threadNum = Integer.parseInt(result.get(i));
                if (threadNum >= halfSize) {
                    secondGroupCount++;
                }
            }
            System.out.printf("처음 %d개 (전체의 %d%%) 중 뒷그룹(%d~) 개수: %d (%.1f%%)%n",
                    checkpoint, ratio, halfSize, secondGroupCount,
                    (double)secondGroupCount/checkpoint*100);
        }
    }
}
/*
비공정
=== 섞임 분석 ===
처음 20개 (전체의 1%) 중 뒷그룹(1000~) 개수: 0 (0.0%)
처음 100개 (전체의 5%) 중 뒷그룹(1000~) 개수: 9 (9.0%)
처음 200개 (전체의 10%) 중 뒷그룹(1000~) 개수: 9 (4.5%)
처음 400개 (전체의 20%) 중 뒷그룹(1000~) 개수: 9 (2.3%)
처음 1000개 (전체의 50%) 중 뒷그룹(1000~) 개수: 9 (0.9%)

공정
=== 섞임 분석 ===
처음 20개 (전체의 1%) 중 뒷그룹(1000~) 개수: 0 (0.0%)
처음 100개 (전체의 5%) 중 뒷그룹(1000~) 개수: 0 (0.0%)
처음 200개 (전체의 10%) 중 뒷그룹(1000~) 개수: 0 (0.0%)
처음 400개 (전체의 20%) 중 뒷그룹(1000~) 개수: 0 (0.0%)
처음 1000개 (전체의 50%) 중 뒷그룹(1000~) 개수: 41 (4.1%)
 */