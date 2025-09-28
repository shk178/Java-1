package memory;

public class StaticData {
    public String name;
    public static int count;
    public StaticData() {
        this.count++;
    }
    void StaticMethod2() {
        this.name = "1";
        this.StaticMethod();
    }
    static void StaticMethod3() {
        count++;
    }
    static void StaticMethod() {
        //this.count--;
        count--;
        //this.name = "1";
        //StaticMethod2();
        StaticMethod3();
    }
}
