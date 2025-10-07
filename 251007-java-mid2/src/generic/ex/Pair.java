package generic.ex;

public class Pair<T, S> {
    private T t;
    private S s;
    public void setFirst(T t) {
        this.t = t;
    }
    public T getFirst() {
        return t;
    }
    public void setSecond(S s) {
        this.s = s;
    }
    public S getSecond() {
        return s;
    }
    public static void main(String[] args) {
        Pair<Integer, String> one = new Pair<>();
        one.setFirst(1);
        one.setSecond("data");
        System.out.println(one.getFirst()); //1
        System.out.println(one.getSecond()); //data
    }
}
