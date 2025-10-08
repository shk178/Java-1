# 4. 컬렉션 프레임워크 - ArrayList
- 1. 배열
```java
public class Main {
    public static void main(String[] args) {
        int[] arr = new int[3];
        //index 입력: O(1)
        arr[0] = 1;
        arr[1] = 2;
        arr[2] = 3;
        //index 변경: O(1)
        arr[2] = 10;
        //index 조회: O(1)
        System.out.println(arr[2]); //10
        //arr 검색: O(n)
        int value = 10;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) break;
        }
    }
}
```
- 메모리 주소는 항상 바이트 단위로 표현
- 각 데이터 타입의 크기만큼 주소가 증가
- 16진수 주소에서 앞의 x(또는 0x)는 16진법을 나타내는 접두사
- arr 주소 = arr[0] 주소 = x100
- arr[1] 주소 = x104
- arr[2] 주소 = x108
- 배열 순차 검색: 배열 크기 n = 연산 최대 n회
- 2. 빅오 표기법
- n은 데이터 크기
- 연산 수 n+2 -> O(n), 연산 수 n/2 -> 0(n)
- O(1): 연산 수가 항상 1 //상수 시간
- O(log n): 연산 수가 log n //로그 시간
- O(n): 연산 수가 n //선형 시간
- O(n log n): n*log n //선형 로그 시간
- O(n^2): n 제곱 //제곱 시간
- n이 클 때: 1 < log n < n < n log n < n^2
- 빅오로 알고리즘 실행 시간 계산보다는
- 데이터 증가에 따른 성능 변화 추세를 본다.
- 3. 배열 데이터 추가
- 배열 위치 i에 데이터 추가
- i부터 arr.length-1까지 오른쪽으로 한 칸씩 이동
- 배열 끝 위치부터 이동해야 데이터 유지
- arr.length-1 -> arr.length로 이동은 런타임 오류 난다. (크기 고정)
- i가 arr.length-1이면 O(1)이다.
- 그외에는 O(n)이다.
- 4. List 자료 구조
- 배열은 크기 정적이고, 데이터 추가 시 이동하는 코드를 작성해야 한다.
- 리스트는 크기 동적이고, 추가 메서드가 있다.
- 배열과 리스트 모두 순서가 있고 중복을 허용한다.