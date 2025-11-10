package basic.lecture;

public class SingletonService {
    //static 영역에 객체를 딱 1개만 생성해둠
    private static final SingletonService instance = new SingletonService();
    //static 메서드로만 조회하도록 허용해둠
    public static SingletonService getInstance() {
        return instance;
    }
    //생성자는 private으로 선언해 외부 객체 생성 막아둠
    private SingletonService() {
    }
    public void logic() {
        System.out.println("싱글톤 객체 로직 호출");
    }
}
