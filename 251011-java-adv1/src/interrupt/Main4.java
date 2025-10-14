package interrupt;

public class Main4 {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                System.out.println("잠들기 전");
                Thread.sleep(10000); //10초 동안 잠듦
                System.out.println("깨어남");
            } catch (InterruptedException e) {
                System.out.println("인터럽트 발생");
            }
        });
        t.start();
        //3초 후 인터럽트
        Thread.sleep(3000);
        t.interrupt();
    }
}
//잠들기 전
//인터럽트 발생