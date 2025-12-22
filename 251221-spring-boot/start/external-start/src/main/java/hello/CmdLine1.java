package hello;

// java -jar app.jar arg1 arg2 key=value
public class CmdLine1 {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("arg = " + arg);
        }
    }
}
/*
arg = dataA
arg = dataB
arg = keyC=valueC
 */