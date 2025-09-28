package memory;
import static memory.StaticData.*;

public class JavaStaticMain {
    public static void main(String[] args) {
        StaticMethod();
        StaticData.StaticMethod();
        //System.out.println("StaticData.name = " + StaticData.name);
        System.out.println("StaticData.count = " + StaticData.count);
        StaticData staticData1 = new StaticData();
        StaticData staticData2 = new StaticData();
        staticData1.StaticMethod();
        System.out.println("staticData1.count = " + staticData2.count);
        System.out.println("staticData2.count = " + staticData2.count);
        //System.out.println("StaticData.name = " + StaticData.name);
        System.out.println("StaticData.count = " + StaticData.count);
    }
}
