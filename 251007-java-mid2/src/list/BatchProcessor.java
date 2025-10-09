package list;

public class BatchProcessor {
    private listInterface<Integer> list;
    public BatchProcessor(listInterface<Integer> list) {
        this.list = list;
    } //런타임에 리스트 타입 결정
    public void setList(listInterface<Integer> list) {
        this.list = list;
    } //런타임에 리스트 타입 결정
    //공통 측정 메서드
    private void measurePerformance(String methodName, Runnable logic, int size) {
        long sTime = System.currentTimeMillis();
        logic.run();
        long eTime = System.currentTimeMillis();
        System.out.print(methodName + " list=" + list.getClass().getName());
        System.out.print(" size=" + size);
        System.out.println(" time=" + (eTime - sTime));
    }
    public void logicFront(int size) {
        measurePerformance("logicFront", () -> {
            for (int i = 0; i < size; i++) {
                list.add(0, i);
            }
        }, size);
    }
    public void logicMiddle(int size) {
        measurePerformance("logicMiddle", () -> {
            for (int i = 0; i < size; i++) {
                list.add(list.size() / 2, i);
            }
        }, size);
    }
    public void logicBack(int size) {
        measurePerformance("logicBack", () -> {
            for (int i = 0; i < size; i++) {
                list.add(list.size(), i);
            }
        }, size);
    }
    public void logicGet(int size) {
        measurePerformance("logicGet", () -> {
            for (int i = 0; i < size; i++) {
                list.get(i);
            }
        }, size);
    }
    public void logicSet(int size) {
        measurePerformance("logicSet", () -> {
            for (int i = 0; i < size; i++) {
                list.set(i, i);
            }
        }, size);
    }
    public void logicIndex(int size) {
        measurePerformance("logicIndex", () -> {
            for (int i = 0; i < size; i++) {
                list.indexOf(i);
            }
        }, size);
    }
}
