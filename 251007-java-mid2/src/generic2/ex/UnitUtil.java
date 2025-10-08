package generic2.ex;

public class UnitUtil {
    public static void main(String[] args) {
        Marine m1 = new Marine("마린1", 40);
        Marine m2 = new Marine("마린2", 50);
        System.out.println(UnitUtil.maxHp(m1, m2)); //class generic2.ex.Marine{name='마린2', hp=50}
        Zealot z1 = new Zealot("질럿1", 100);
        Zealot z2 = new Zealot("질럿2", 150);
        System.out.println(UnitUtil.maxHp(z1, z2)); //class generic2.ex.Zealot{name='질럿2', hp=150}
        UnitUtil.maxHp(z1, z2).zealotOnly(); //zealot
        System.out.println(getMarine(m1)); //class generic2.ex.Marine{name='마린1', hp=40}
        getMarine(m1).marineOnly(); //marine
        System.out.println(getZealot(z1)); //class generic2.ex.Zealot{name='질럿1', hp=100}
        getZealot(z1).zealotOnly(); //zealot
        System.out.println(getZergling(new Zergling("저글링", 10))); //class generic2.ex.Zergling{name='저글링', hp=10}
        //getZergling(new Zergling("저글링", 10)).zerglingOnly();
        //java: cannot find symbol
        //  symbol:   method zerglingOnly()
        //  location: class generic2.ex.BioUnit
    }
    private static <T extends BioUnit> T maxHp(T t1, T t2) {
        return t1.getHp() > t2.getHp() ? t1 : t2;
    }
    private static Marine getMarine(BioUnit unit) {
        return (Marine) unit;
    }
    private static Zealot getZealot(Zealot z) {
        return z;
    }
    private static BioUnit getZergling(Zergling z) {
        return z;
    }
}
