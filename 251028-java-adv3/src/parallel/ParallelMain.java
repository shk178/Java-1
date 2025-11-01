package parallel;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class ParallelMain {
    static class SumTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 3;
        private final List<Integer> list;
        public SumTask(List<Integer> list) {
            this.list = list;
        }
        @Override
        protected Integer compute() {
            if (list.size() <= THRESHOLD) {
                int sum = list.stream()
                        .mapToInt(HeavyJob::heavyTask)
                        .sum();
                MyLogger.log("sum=" + sum + "(" + list + ")");
                return sum;
            }
            int mid = list.size() / 2;
            List<Integer> leftList = list.subList(0, mid);
            List<Integer> rightList = list.subList(mid, list.size());
            SumTask leftTask = new SumTask(leftList);
            SumTask rightTask = new SumTask(rightList);
            leftTask.fork();
            int rightResult = rightTask.compute();
            String msg = "leftTask=" + list.subList(0, mid) + ", rightTask=" + list.subList(mid, list.size()) + " (" + rightResult + ")";
            MyLogger.log(msg);
            int leftResult = leftTask.join();
            int joinSum = leftResult + rightResult;
            return joinSum;
        }
    }
    public static void main(String[] args) {
        List<Integer> data = IntStream.rangeClosed(1, 8)
                .boxed()
                .toList();
        long sTime = System.currentTimeMillis();
        ForkJoinPool fjPool = new ForkJoinPool();
        SumTask task = new SumTask(data);
        int result = fjPool.invoke(task);
        fjPool.close();
        long eTime = System.currentTimeMillis();
        System.out.println("합계=" + result + ", 시간=" + (eTime - sTime));
    }
}
/*
14:28:23.637 [ForkJoinPool-1-worker-2] calculate 3 -> 30
14:28:23.637 [ForkJoinPool-1-worker-3] calculate 5 -> 50
14:28:23.637 [ForkJoinPool-1-worker-4] calculate 1 -> 10
14:28:23.637 [ForkJoinPool-1-worker-1] calculate 7 -> 70
14:28:24.643 [ForkJoinPool-1-worker-3] calculate 6 -> 60
14:28:24.643 [ForkJoinPool-1-worker-4] calculate 2 -> 20
14:28:24.643 [ForkJoinPool-1-worker-1] calculate 8 -> 80
14:28:24.643 [ForkJoinPool-1-worker-2] calculate 4 -> 40
14:28:25.657 [ForkJoinPool-1-worker-2] sum=70([3, 4])
14:28:25.657 [ForkJoinPool-1-worker-4] sum=30([1, 2])
14:28:25.657 [ForkJoinPool-1-worker-3] sum=110([5, 6])
14:28:25.657 [ForkJoinPool-1-worker-1] sum=150([7, 8])
14:28:25.663 [ForkJoinPool-1-worker-2] leftTask=[1, 2], rightTask=[3, 4] (70)
14:28:25.663 [ForkJoinPool-1-worker-1] leftTask=[5, 6], rightTask=[7, 8] (150)
14:28:25.663 [ForkJoinPool-1-worker-1] leftTask=[1, 2, 3, 4], rightTask=[5, 6, 7, 8] (260)
합계=360, 시간=2053
 */
/*
14:43:54.361 [ForkJoinPool-1-worker-4] calculate 1 -> 10
14:43:54.361 [ForkJoinPool-1-worker-3] calculate 5 -> 50
14:43:54.361 [ForkJoinPool-1-worker-1] calculate 7 -> 70
14:43:54.361 [ForkJoinPool-1-worker-2] calculate 3 -> 30
14:43:55.369 [ForkJoinPool-1-worker-4] calculate 2 -> 20
14:43:55.369 [ForkJoinPool-1-worker-3] calculate 6 -> 60
14:43:55.369 [ForkJoinPool-1-worker-2] calculate 4 -> 40
14:43:55.369 [ForkJoinPool-1-worker-1] calculate 8 -> 80
14:43:56.380 [ForkJoinPool-1-worker-2] sum=70([3, 4])
14:43:56.380 [ForkJoinPool-1-worker-3] sum=110([5, 6])
14:43:56.380 [ForkJoinPool-1-worker-4] sum=30([1, 2])
14:43:56.380 [ForkJoinPool-1-worker-1] sum=150([7, 8])
14:43:56.386 [ForkJoinPool-1-worker-1] leftTask=[5, 6], rightTask=[7, 8] (150)
14:43:56.386 [ForkJoinPool-1-worker-2] leftTask=[1, 2], rightTask=[3, 4] (70)
14:43:56.386 [ForkJoinPool-1-worker-1] leftTask=[1, 2, 3, 4], rightTask=[5, 6, 7, 8] (260)
합계=360, 시간=2058
 */