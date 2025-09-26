package array;

public class Array2 {
    public static void main(String[] args) {
        int[] students; //int 배열 변수 선언
        students = new int[5]; //배열 생성
        students[0] = 90; //1. 배열에 값을 대입
        /*
        x001[0] = 90;
        1-1. 변수에 있는 참조값을 통해 실제 배열에 접근
        1-2. 인덱스를 사용해서 해당 위치의 요소에 접근
        1-3. 값 대입
         */
        students[1] = 80; //마찬가지
    }
}
