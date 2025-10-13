package control;

import static thread.MyLogger.log;

public class ThreadMain {
    public static void main(String[] args) {
        Thread maint = Thread.currentThread();
        //자바 프로그램은 항상 main 스레드에서 시작
        log(maint);
        //15:56:35.809 [     main] Thread[#1,main,5,main]
        //#1: 스레드 ID (JVM 내에서 유일한 번호)
        //main: 스레드 이름
        //5: 스레드 우선순위 (기본값 5)
        //main: 스레드가 속한 그룹 이름
        log(maint.threadId());
        //15:56:35.811 [     main] 1
        //스레드 id 고유
        log(maint.getName());
        //15:56:35.811 [     main] main
        //스레드 이름 중복 가능
        log(maint.getPriority());
        //15:56:35.813 [     main] 5
        //1~10 우선순위 (순서 보장은 x)
        log(maint.getThreadGroup());
        //15:56:35.813 [     main] java.lang.ThreadGroup[name=main,maxpri=10]
        //기본적으로 모든 스레드는 main 그룹에 속함
        //maxpri=10은 이 그룹 내에서 가질 수 있는 최대 우선순위
        log(maint.getState());
        //15:56:35.813 [     main] RUNNABLE
        //스레드의 현재 상태를 반환
        //상태는 Thread.State enum으로 정의되어 있다.
    }
}
