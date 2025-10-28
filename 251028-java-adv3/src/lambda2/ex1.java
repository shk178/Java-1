package lambda2;

public class ex1 {
    public static void main(String[] args) {
        StringFunction kg = (String nkg) -> {
            String n = nkg.substring(0, nkg.indexOf("kg"));
            System.out.println("무게: " + n + "kg");
        };
        StringFunction g = (String ng) -> {
            String n = ng.substring(0, ng.indexOf("g"));
            System.out.println("무게: " + n + "g");
        };
        kg.apply("100kg"); // 무게: 100kg
        g.apply("10g"); // 무게: 10g
    }
}
