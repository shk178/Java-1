package parallel;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

// 배열의 합을 병렬로 계산하는 예제
public class SumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 10; // 분할 임계값
    private long[] array;
    private int start;
    private int end;

    public SumTask(long[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;

        // 작업이 충분히 작으면 직접 계산
        if (length <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        }

        // 작업을 두 개로 분할
        int mid = start + length / 2;
        SumTask leftTask = new SumTask(array, start, mid);
        SumTask rightTask = new SumTask(array, mid, end);
        String msg1 = "leftTask=" + start + "~" + mid + ", rightTask=" + mid + "~" + end;
        MyLogger.log(msg1);

        // 왼쪽 작업을 비동기로 실행
        leftTask.fork();

        // 오른쪽 작업을 현재 스레드에서 실행
        long rightResult = rightTask.compute(); // return leftResult + rightResult;이 실행되어야 출력됨
        String msg2 = "leftTask=" + start + "~" + mid + ", rightTask=" + mid + "~" + end + " (" + rightResult + ")";
        MyLogger.log(msg2);

        // 왼쪽 작업의 결과를 기다림
        long leftResult = leftTask.join();

        // 결과 병합
        return leftResult + rightResult;
    }

    public static void main(String[] args) {
        long[] array = new long[100];
        // 배열 초기화
        array = initArr(array);

        ForkJoinPool pool = new ForkJoinPool();
        SumTask task = new SumTask(array, 0, array.length);
        Long result = pool.invoke(task);

        System.out.println("합계: " + result); // 합계: 100000
    }

    static long[] initArr(long[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 1;
        }
        return arr;
    }
}
/*
13:50:48.012 [ForkJoinPool-1-worker-1] leftTask=0~50, rightTask=50~100
13:50:48.014 [ForkJoinPool-1-worker-1] leftTask=50~75, rightTask=75~100
13:50:48.014 [ForkJoinPool-1-worker-1] leftTask=75~87, rightTask=87~100
13:50:48.014 [ForkJoinPool-1-worker-2] leftTask=0~25, rightTask=25~50
13:50:48.014 [ForkJoinPool-1-worker-3] leftTask=50~62, rightTask=62~75
13:50:48.014 [ForkJoinPool-1-worker-1] leftTask=87~93, rightTask=93~100
13:50:48.014 [ForkJoinPool-1-worker-5] leftTask=50~56, rightTask=56~62
13:50:48.014 [ForkJoinPool-1-worker-2] leftTask=25~37, rightTask=37~50
13:50:48.014 [ForkJoinPool-1-worker-4] leftTask=75~81, rightTask=81~87
13:50:48.014 [ForkJoinPool-1-worker-3] leftTask=62~68, rightTask=68~75
13:50:48.016 [ForkJoinPool-1-worker-6] leftTask=0~12, rightTask=12~25
13:50:48.016 [ForkJoinPool-1-worker-2] leftTask=37~43, rightTask=43~50
13:50:48.016 [ForkJoinPool-1-worker-7] leftTask=25~31, rightTask=31~37
13:50:48.016 [ForkJoinPool-1-worker-6] leftTask=12~18, rightTask=18~25
13:50:48.016 [ForkJoinPool-1-worker-8] leftTask=0~6, rightTask=6~12
13:50:48.025 [ForkJoinPool-1-worker-8] leftTask=0~6, rightTask=6~12 (6)
13:50:48.025 [ForkJoinPool-1-worker-6] leftTask=12~18, rightTask=18~25 (7)
13:50:48.025 [ForkJoinPool-1-worker-3] leftTask=62~68, rightTask=68~75 (7)
13:50:48.025 [ForkJoinPool-1-worker-5] leftTask=50~56, rightTask=56~62 (6)
13:50:48.026 [ForkJoinPool-1-worker-4] leftTask=75~81, rightTask=81~87 (6)
13:50:48.026 [ForkJoinPool-1-worker-7] leftTask=25~31, rightTask=31~37 (6)
13:50:48.026 [ForkJoinPool-1-worker-6] leftTask=0~12, rightTask=12~25 (13)
13:50:48.026 [ForkJoinPool-1-worker-2] leftTask=37~43, rightTask=43~50 (7)
13:50:48.026 [ForkJoinPool-1-worker-1] leftTask=87~93, rightTask=93~100 (7)
13:50:48.026 [ForkJoinPool-1-worker-3] leftTask=50~62, rightTask=62~75 (13)
13:50:48.027 [ForkJoinPool-1-worker-2] leftTask=25~37, rightTask=37~50 (13)
13:50:48.027 [ForkJoinPool-1-worker-1] leftTask=75~87, rightTask=87~100 (13)
13:50:48.028 [ForkJoinPool-1-worker-2] leftTask=0~25, rightTask=25~50 (25)
13:50:48.028 [ForkJoinPool-1-worker-1] leftTask=50~75, rightTask=75~100 (25)
13:50:48.028 [ForkJoinPool-1-worker-1] leftTask=0~50, rightTask=50~100 (50)
합계: 100
 */