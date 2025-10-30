package lambda4;

public class CaptureMain {
    public static void main(String[] args) {
        final int finalVar = 10;
        int effectivelyFinalVar = 20;
        int notFinalVar = 30;
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
                System.out.println(finalVar);
                System.out.println(effectivelyFinalVar);
                //System.out.println(notFinalVar);
            }
        };
        Runnable lambda = () -> {
            System.out.println(finalVar);
            System.out.println(effectivelyFinalVar);
            //System.out.println(notFinalVar);
        };
        notFinalVar += 1;
        anonymous.run();
        lambda.run();
        //10
        //20
        //10
        //20
    }
}
