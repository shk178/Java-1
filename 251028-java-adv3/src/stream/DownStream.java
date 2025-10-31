package stream;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownStream {
    static class Student {
        private String name;
        private int grade;
        private int score;
        public Student(String name, int grade, int score) {
            this.name = name;
            this.grade = grade;
            this.score = score;
        }
        @SuppressWarnings("unchecked")
        public <T> T get(String fieldName) {
            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                field.setAccessible(true); // private 필드 접근 허용
                return (T) field.get(this); // 현재 객체에서 필드 값 반환
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student("K", 1, 85),
                new Student("L", 1, 70),
                new Student("P", 2, 90),
                new Student("H", 2, 70),
                new Student("C", 3, 95),
                new Student("G", 3, 70)
        );
        Map<Integer, List<Student>> gradeGroup = students.stream()
                .collect(Collectors.groupingBy(
                        s -> s.get("grade"),
                        Collectors.toList() // 생략 가능
                        // groupingBy()의 두 번째 인자의 기본값이 Collectors.toList()
                ));
        // groupingBy의 두 번째 파라미터(다운스트림)를 생략하면
        // 컴파일러가 타입을 추론할 때 첫 번째 파라미터만 보고 판단
        Map<Integer, List<String>> gradeName = students.stream()
                .collect(Collectors.groupingBy(
                        //s -> s.get("grade"), // 여기서 s의 타입이 Student인 건 알지만
                        // s.get("grade")의 반환 타입이 Integer인지 컴파일러가 확신할 수 없음
                        //s -> (Integer) s.get("grade"), // 명시적 캐스팅 가능 - 추론 에러 발생
                        // 하지만 제네릭 메서드 대신 일반 getter를 만드는 게 낫다.
                        //s -> s.<Integer>get("grade"), // 타입 힌트도 가능 - 추론 에러 발생
                        s -> s.get("grade"),
                        /* Collectors.groupingBy( // 추론 에러 해결 - mapping으로 바꾼다. */
                        Collectors.mapping(
                                s -> s.get("name"), // 여기까지 쓰면 타입 추론 가능 - 추론 에러 발생
                                Collectors.toList()
                        )
                        /*
                        inference variable D has incompatible equality constraints java.util.List<java.lang.String>, java.util.Map<K,D>
                            D는 groupingBy의 결과 값 타입을 나타내는 타입 변수
                            컴파일러가 D를 List<String>으로 추론하려고 함
                            동시에 D를 Map<K, D>의 일부로도 추론하려고 함
                            이 두 가지가 서로 맞지 않아서 에러 발생
                            캐스팅이나 타입 힌트를 추가해도 문제는 해결되지 않는다.
                            (Integer) s.get("grade") 덕분에 키가 Integer인 건 명확해짐
                            첫 번째 groupingBy:
                                입력: Stream<Student>
                                키: s.get("grade") → Integer
                                값: 두 번째 groupingBy의 결과
                            두 번째 groupingBy (다운스트림):
                                입력: 각 그룹의 Student들
                                키: s.get("name") → String
                                값: Collectors.toList() → List<Student>
                                결과: Map<String, List<Student>>
                            전체 결과가 이렇게 나와버린다: Map<Integer, Map<String, List<Student>>>
                         */
                        /*
                        해결 방법:
                        // 학년 → 학생 이름 리스트
                        // {1=[K, L], 2=[P, H], 3=[C, G]}
                        Map<Integer, List<String>> gradeName = students.stream()
                            .collect(Collectors.groupingBy(
                                s -> s.<Integer>get("grade"),     // 학년별로 그룹화
                                Collectors.mapping(                // Student를 String으로 변환
                                    s -> s.<String>get("name"),
                                    Collectors.toList()
                                )
                            ));
                        //또는 중첩 그룹화를 원한다면 - 학년 → 이름 → 학생 리스트
                        // {1={K=[학생], L=[학생]}, 2={P=[학생], H=[학생]}}
                        Map<Integer, Map<String, List<Student>>> nestedGroup = students.stream()
                            .collect(Collectors.groupingBy(
                                s -> s.<Integer>get("grade"),     // 학년별로
                                Collectors.groupingBy(             // 이름별로 다시 그룹화
                                    s -> s.<String>get("name"),
                                    Collectors.toList()
                                )
                            ));
                        // groupingBy는 항상 Map을 반환
                        // mapping은 요소를 변환
                         */
                ));
        // 제네릭 메서드도 메서드 참조가 가능 - 하지만 타입 파라미터를 명시할 수 없다는 제약
        // 제네릭 메서드 - 람다식에서는 타입 파라미터 명시 가능 s -> s.<String>get("name")
        // 제네릭 메서드 참조 - 컨텍스트에서 타입 추론이 가능하면 작동
        /*
        List<Box<String>> boxes = ...;
        List<String> values = boxes.stream()
                .map(Box::getValue)  // 반환 타입이 String으로 추론됨
                .collect(Collectors.toList());
         */
        // 메서드 참조와 매개변수 - 메서드 참조는 매개변수를 직접 전달할 수 없다.
        // 메서드 참조는 메서드 자체를 가리키는 거지, 메서드 호출이 아니다.
        // 람다식 - 매개변수 전달 가능
        System.out.println(gradeGroup);
        System.out.println(gradeName);
    }
}
//{1=[stream.DownStream$Student@66a29884, stream.DownStream$Student@4769b07b], 2=[stream.DownStream$Student@cc34f4d, stream.DownStream$Student@17a7cec2], 3=[stream.DownStream$Student@65b3120a, stream.DownStream$Student@6f539caf]}
//{1=[K, L], 2=[P, H], 3=[C, G]}