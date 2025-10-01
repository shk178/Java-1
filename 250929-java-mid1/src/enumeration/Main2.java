package enumeration;

public class Main2 {
    public static void main(String[] args) {
        System.out.println(Grade.BASIC); //BASIC
        System.out.println(Grade.GOLD); //GOLD
        System.out.println(Grade.DIA); //DIA
        System.out.println(System.identityHashCode(Grade.BASIC)); //793589513
        System.out.println(System.identityHashCode(Grade.GOLD)); //1313922862
        System.out.println(System.identityHashCode(Grade.DIA)); //495053715
        System.out.println(Grade.BASIC.hashCode()); //793589513
        System.out.println(Grade.GOLD.hashCode()); //1313922862
        System.out.println(Grade.DIA.hashCode()); //495053715
        System.out.println(Grade.BASIC.ordinal()); //0
        System.out.println(Grade.GOLD.ordinal()); //1
        System.out.println(Grade.DIA.ordinal()); //2
    }
}
