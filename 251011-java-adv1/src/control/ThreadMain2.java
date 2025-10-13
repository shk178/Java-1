package control;

import static thread.MyLogger.log;

public class ThreadMain2 {
    public static void main(String[] args) {
        Thread myt = new Thread(new Runnable() {
            @Override
            public void run() {
                log("run()");
            }
        });
        log(myt); //16:12:59.886 [     main] Thread[#22,Thread-0,5,main]
        log(myt.threadId()); //16:12:59.889 [     main] 22
        log(myt.getName()); //16:12:59.889 [     main] Thread-0
        log(myt.getPriority()); //16:12:59.889 [     main] 5
        log(myt.getThreadGroup()); //16:12:59.890 [     main] java.lang.ThreadGroup[name=main,maxpri=10]
        log(myt.getState()); //16:12:59.890 [     main] NEW
    }
}
