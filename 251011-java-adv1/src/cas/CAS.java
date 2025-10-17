package cas;

import java.util.concurrent.atomic.AtomicInteger;

public class CAS {
    static AtomicInteger value = new AtomicInteger(0);
    public static void main(String[] args) throws InterruptedException {
        Thread threadB = new Thread(() -> {
            while (true) {
                int newVal = value.incrementAndGet(); //CAS 기반 증가
                System.out.println("B가 값 변경 → " + newVal);
                try {
                    Thread.sleep(1); //너무 빠르면 로그가 폭주하니까 딜레이
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        Thread threadA = new Thread(() -> {
            try {
                Thread.sleep(10); //Thread B가 먼저 값을 바꾸도록 딜레이
            } catch (InterruptedException e) {
                return;
            }
            int retries = 0;
            while (true) {
                int old = value.get();
                int newVal = old + 1;
                boolean success = value.compareAndSet(old, newVal);
                if (success) {
                    System.out.println("A 성공: " + old + " → " + newVal + " (재시도 횟수: " + retries + ")");
                    break;
                } else {
                    retries++;
                    System.out.println("A 실패... 기대값=" + old + ", 실제값=" + value.get());
                }
            }
        });
        threadB.start();
        threadA.start();
        //메인 스레드가 너무 빨리 끝나지 않게 대기
        Thread.sleep(2000);
        threadB.interrupt();
    }
}
/*
B가 값 변경 → 1
B가 값 변경 → 2
B가 값 변경 → 3
B가 값 변경 → 4
B가 값 변경 → 5
B가 값 변경 → 6
B가 값 변경 → 7
B가 값 변경 → 8
B가 값 변경 → 9
B가 값 변경 → 10
B가 값 변경 → 12
B가 값 변경 → 13
B가 값 변경 → 14
B가 값 변경 → 15
A 성공: 10 → 11 (재시도 횟수: 0)
//B가 값 변경 계속됨
 */