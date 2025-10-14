package yield;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class SimpleWorker extends Thread {
    private Queue<String> jobQueue;
    public SimpleWorker(Queue<String> jobQueue, String name) {
        super(name);
        this.jobQueue = jobQueue;
    }
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) { //5번 반복
            if (jobQueue.isEmpty()) {
                System.out.println(getName() + ": 큐가 비어있음, CPU 양보 - 작업" + i + "를 큐에 추가");
                jobQueue.add("작업" + i + "(" + getName() + "가 추가)");
                System.out.println(getName() + ": yield() 실행 - 현재 루프 i=" + i);
                Thread.yield(); //다른 스레드에게 양보
                continue;
            }
            System.out.println(getName() + ": poll() 실행 - 현재 루프 i=" + i);
            String job = jobQueue.poll();
            System.out.println(getName() + ": " + job + " 처리 - 현재 루프 i=" + i);
        }
    }
    public static void main(String[] args) {
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();
        //스레드 3개 시작
        new SimpleWorker(jobQueue, "워커a").start();
        new SimpleWorker(jobQueue, "워커b").start();
        new SimpleWorker(jobQueue, "워커c").start();
    }
}
/*
워커b: 큐가 비어있음, CPU 양보 - 작업0를 큐에 추가
워커a: 큐가 비어있음, CPU 양보 - 작업0를 큐에 추가
워커c: 큐가 비어있음, CPU 양보 - 작업0를 큐에 추가
워커c: yield() 실행 - 현재 루프 i=0
워커a: yield() 실행 - 현재 루프 i=0
워커b: yield() 실행 - 현재 루프 i=0
워커c: poll() 실행 - 현재 루프 i=1
워커a: poll() 실행 - 현재 루프 i=1
워커b: poll() 실행 - 현재 루프 i=1
워커b: 작업0(워커b가 추가) 처리 - 현재 루프 i=1
워커a: 작업0(워커c가 추가) 처리 - 현재 루프 i=1
워커a: 큐가 비어있음, CPU 양보 - 작업2를 큐에 추가
워커b: 큐가 비어있음, CPU 양보 - 작업2를 큐에 추가
워커a: yield() 실행 - 현재 루프 i=2
워커b: yield() 실행 - 현재 루프 i=2
워커a: poll() 실행 - 현재 루프 i=3
워커c: 작업0(워커a가 추가) 처리 - 현재 루프 i=1
워커b: poll() 실행 - 현재 루프 i=3
워커a: 작업2(워커a가 추가) 처리 - 현재 루프 i=3
워커c: poll() 실행 - 현재 루프 i=2
워커b: 작업2(워커b가 추가) 처리 - 현재 루프 i=3
워커a: 큐가 비어있음, CPU 양보 - 작업4를 큐에 추가
워커c: null 처리 - 현재 루프 i=2
워커b: 큐가 비어있음, CPU 양보 - 작업4를 큐에 추가
워커a: yield() 실행 - 현재 루프 i=4
워커c: poll() 실행 - 현재 루프 i=3
워커c: 작업4(워커a가 추가) 처리 - 현재 루프 i=3
워커b: yield() 실행 - 현재 루프 i=4
워커c: poll() 실행 - 현재 루프 i=4
워커c: 작업4(워커b가 추가) 처리 - 현재 루프 i=4
 */