package access.ex;

public class MaxCounter {
    private int count = 0;
    private int max;
    public MaxCounter(int max) {
        this.max = max;
    }
    public void increment() {
        if (isValid()) {
            this.count++;
        } else {
            System.out.println("최댓값 도달하여 증가x");
        }
    }
    private boolean isValid() {
        return this.count < this.max;
    }
    public int getCount() {
        return this.count;
    }
}
