package annotation;

public class Main3 {
    public static void main(String[] args) {
        User user = new User("user1", 0);
        Team team = new Team("", 0);
        try {
            Validator.validate(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //java.lang.RuntimeException: 유저 나이는 1~100 사이
        //	at annotation.Validator.validate(Validator.java:22)
        //	at annotation.Main3.main(Main3.java:8)
        try {
            Validator.validate(team);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //java.lang.RuntimeException: 회원 수는 1~999 사이
        //	at annotation.Validator.validate(Validator.java:22)
        //	at annotation.Main3.main(Main3.java:16)
        //팀 이름이 비어 있음
    }
}
