package system;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        //현재 시간(밀리초)를 가져온다.
        long currentTimeMillis = System.currentTimeMillis();
        System.out.println("currentTimeMillis = " + currentTimeMillis); //currentTimeMillis = 1759311877255
        //현재 시간(나노초)를 가져온다.
        long currentNanoTime = System.nanoTime();
        System.out.println("currentNanoTime = " + currentNanoTime); //currentNanoTime = 294160339001800
        //OS: 환경 변수를 읽는다.
        System.out.println("System.getenv() = " + System.getenv());
        //System.getenv() = {USERDOMAIN_ROAMINGPROFILE=DESKTOP-N7N841G, LOCALAPPDATA=C:\Users ...
        //Java: 시스템 속성을 읽는다.
        System.out.println("System.getProperties() = " + System.getProperties());
        //System.getProperties() = {java.specification.version=21, sun.cpu.isalist=amd64, sun.jnu.encoding=MS949, ...
        System.out.println("java.version = " + System.getProperty("java.version")); //java.version = 21.0.8
        //OS: 배열을 고속으로 복사한다.
        char[] srcArr = {'a', 'b', 'c'};
        char[] destArr = new char[srcArr.length];
        System.arraycopy(srcArr, 0, destArr, 0, srcArr.length);
        System.out.println(Arrays.toString(destArr)); //[a, b, c]
    }
}
