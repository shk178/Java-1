package except1;

public class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }
}
//Exception 클래스를 상속받는 새 클래스 CustomException을 정의
//CustomException은 체크 예외(Checked Exception) 로 동작
//컴파일러가 이 예외를 처리하거나 throws로 던져야 한다고 강제
//public CustomException(String message)
//생성자(Constructor)
//예외 객체를 만들 때, 예외 메시지를 문자열로 받도록 한다.
//super(message)
//예외 메시지를 부모 클래스에 전달해서
//getMessage() 메서드로 그 메시지를 꺼낼 수 있게 한다.
//사용 예시
/*
public class Example {
    public static void main(String[] args) {
        try {
            checkAge(15);
        } catch (CustomException e) {
            System.out.println("예외 발생: " + e.getMessage());
        }
    }
    static void checkAge(int age) throws CustomException {
        if (age < 20) {
            throw new CustomException("20세 이상 아님");
        }
    }
}
 */
//Throwable, Exception 클래스 단순화 버전
/*
public class Throwable implements Serializable {
    private String detailMessage; //예외 메시지가 실제로 저장되는 곳
    public Throwable(String message) {
        detailMessage = message;
    }
    public String getMessage() {
        return detailMessage;
    }
}
public class Exception extends Throwable {
    public Exception(String message) {
        super(message); //Throwable(String message) 호출
    }
}
 */
//detailMessage는 private으로 되어 있다.
//Exception, CustomException에서 직접 접근 안 된다.
//같은 필드를 자식 클래스에 선언하면, 부모 타입으로 받았을 때 다르게 호출된다.