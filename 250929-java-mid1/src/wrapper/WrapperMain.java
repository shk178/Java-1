package wrapper;

public class WrapperMain {
    public static void main(String[] args) {
        Integer i1 = new Integer(10);
        //항상 새로운 객체 생성
        Integer i2 = Integer.valueOf(10);
        //-128 ~ 127 범위의 값은 캐싱되어 재사용
        //같은 값에 대해 같은 객체를 반환
        Integer i3 = 10;
        //auto-boxing (내부적으로 valueOf() 호출)
        System.out.println("i1 == i2 = " + (i1 == i2)); //false
        System.out.println("i1 == i3 = " + (i1 == i3)); //false
        System.out.println("i2 == i3 = " + (i2 == i3)); //true
        System.out.println("i3 = " + i3); //10
        //참조값 대신 내부 값을 출력하도록 toString()을 재정의
        int i4 = i3.intValue();
        System.out.println("i4 = " + i4); //10
        //명시적 언박싱: 객체 내부의 기본형 값을 반환
        int i5 = i3;
        System.out.println("i5 = " + i5); //10
        //자동 언박싱: 내부적으로 intValue() 호출
    }
}
