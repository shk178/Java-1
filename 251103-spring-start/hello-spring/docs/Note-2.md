# 4. 회원 관리 예제 - 백엔드 개발
## 비즈니스 요구사항 정리
- 데이터: 회원ID, 이름
- 기능: 회원등록, 조회
- 데이터 저장소는 아직 선정 안 됨
- 일반적인 웹 애플리케이션 계층 구조
```
컨트롤러 -> 서비스 -> 리포지토리 -> DB
컨트롤러 -> 도메인
서비스 -> 도메인
리포지토리 -> 도메인
// 컨트롤러 = 웹 MVC의 컨트롤러 역할
// 서비스 = 핵심 비즈니스 로직 구현
// 리포지토리 = 데이터베이스에 접근, 도메인 객체를 DB에 저장하고 관리
// 도메인 = 비즈니스 도메인 객체 예) 회원, 주문, 쿠폰 등 주로 데이터베이스에 저장하고 관리됨
```
- 클래스 의존관계
```
MemberService 클래스 ㅡ> MemberRepository 인터페이스
MemoryMemberRepository 클래스 --> MemberRepository 인터페이스
// 초기 개발 단계에서는 구현체로 가벼운 메모리 기반의 데이터 저장소 사용
```
## 회원 도메인과 리포지토리 만들기
- 회원 객체
```java
package hello.hellospring.domain;

public class Member {
    private Long id; //Long은 long의 래퍼 클래스
    private String name;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
```
- 회원 리포지토리 인터페이스
```java
package hello.hellospring.repository;
import hello.hellospring.domain.Member;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByName(String name);
    List<Member> findAll();
}
```
- 회원 리포지토리 메모리 구현체
```java
package hello.hellospring.repository;
import hello.hellospring.domain.Member;
//동시성 문제 해결 안 됨
//실무에서는 ConcurrentHashMap, AtomicLong 사용 고려
public class MemoryMemberRepository implements MemberRepository {
    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;
    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }
    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }
    public void clearStore() {
        store.clear();
    }
}
```
## 회원 리포지토리 테스트 케이스 작성
- 개발한 기능 테스트할 때 자바의 main 메서드 사용하거나, 웹 애플리케이션의 컨트롤러 사용한다.
- 이러한 방법은 준비-실행 시간이 길고, 반복 실행 어렵고, 여러 테스트 한 번에 실행 어렵다.
- 자바는 JUnit이라는 프레임워크로 테스트를 실행해 이러한 문제 해결한다.
- 회원 리포지토리 메모리 구현체 테스트

```java
// "src/test/java" 하위 폴더에 생성
package hello.hellospring.repository;
import hello.hellospring.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MemoryMemberRepositoryTest {
    MemoryMemberRepository repository = new MemoryMemberRepository();
    @AfterEach // 각 테스트 종료 때마다 실행
    public void afterEach() {
        repository.clearStore();
    }
    @Test // 테스트는 독립적 실행 (순서 의존x)
    public void save() {
        //given
        Member member = new Member();
        member.setName("spring");
        //when
        repository.save(member);
        //then
        Member result = repository.findById(member.getId()).get();
        assertThat(result).isEqualTo(member);
    }
    @Test
    public void findByName() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);
        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);
        //when
        Member result = repository.findByName("spring1").get();
        //then
        assertThat(result).isEqualTo(member1);
    }
    @Test
    public void findAll() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);
        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);
        //when
        List<Member> result = repository.findAll();
        //then
        assertThat(result.size()).isEqualTo(2);
    }
}
```
## 회원 서비스 개발
```java
package hello.hellospring.service;
import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;

public class MemberService {
    private final MemberRepository memberRepository;
    //회원 서비스 코드를 DI 가능하도록
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    //회원 가입
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 검증
        memberRepository.save(member);
        return member.getId();
    }
    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원");
                });
    }
    //전체 회원 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```
## 회원 서비스 테스트

```java
package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceTest {
    MemberService memberService;
    MemoryMemberRepository memberRepository;

    @BeforeEach
    public void beforeEach() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);
    }

    @AfterEach
    public void afterEach() {
        memberRepository.clearStore();
    }

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("hello");
        //when
        Long saveId = memberService.join(member);
        //then
        Member findMember = memberRepository.findById(saveId).get();
        assertEquals(member.getName(), findMember.getName());
    }

    @Test
    public void 중복회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("spring");
        Member member2 = new Member();
        member2.setName("spring");
        //when
        memberService.join(member1);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> memberService.join(member2));
        //then
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원");
    }
}
```
# 회원 관리 예제 코드 분석
## 1. 전체 아키텍처 구조
### 계층 구조
```
컨트롤러 (Controller) → 서비스 (Service) → 리포지토리 (Repository) → 데이터베이스 (DB)
                              ↓
                           도메인 (Domain)
```
### 설계 원칙
- 관심사의 분리 (Separation of Concerns): 각 계층이 명확한 책임을 가짐
- 의존성 역전 원칙 (DIP): 서비스는 구체적인 구현체가 아닌 인터페이스에 의존
- 단일 책임 원칙 (SRP): 각 클래스는 하나의 책임만 가짐
## 2. 도메인 레이어: Member 클래스
### 파일 위치
```1:22:src/main/java/hello/hello_spring/domain/Member.java
package hello.hello_spring.domain;

public class Member {
    private Long id; //Long은 long의 래퍼 클래스
    private String name;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
```
### 작동 원리
- Entity/Value Object: 비즈니스 도메인의 핵심 데이터를 표현하는 순수한 자바 클래스
- 캡슐화: private 필드와 public getter/setter를 통해 데이터 접근 제어
- JavaBean 패턴: 표준 자바빈 규칙을 따름
### 설계 이유
1. 도메인 중심 설계: 비즈니스 로직의 핵심인 회원 데이터를 독립적으로 관리
2. 재사용성: 다양한 계층(서비스, 리포지토리, 컨트롤러)에서 사용 가능
3. 유지보수성: 도메인 로직 변경 시 한 곳만 수정하면 됨
### Import 패키지
- 패키지 선언만 사용: 도메인 클래스는 순수한 자바 객체이므로 외부 의존성 없음
### Long vs long
- Long: 래퍼 클래스, null 값 가능, 객체 타입
    - 데이터베이스와의 매핑에서 null 처리 가능
    - Optional과 함께 사용하기 적합
- long: 원시 타입, null 불가능, 기본 타입
    - 성능상 약간 유리하지만 null 처리 불가
## 3. 리포지토리 레이어: MemberRepository 인터페이스
### 파일 위치
```1:12:src/main/java/hello/hello_spring/repository/MemberRepository.java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByName(String name);
    List<Member> findAll();
}
```
### 작동 원리
- 인터페이스: 구현체에 대한 계약을 정의
- 추상화: 구체적인 저장소 구현 방식에 독립적
- 다형성: 하나의 인터페이스로 여러 구현체 사용 가능
### 설계 이유
1. 의존성 역전 원칙 (DIP):
    - 서비스 계층이 구체적인 구현체가 아닌 인터페이스에 의존
    - 데이터 저장소 변경 시 서비스 코드 수정 불필요
2. 확장성:
    - 메모리 기반 → DB 기반으로 쉽게 전환 가능
    - JPA, JDBC, MongoDB 등 다양한 구현체 교체 가능
3. 테스트 용이성:
    - Mock 객체나 테스트용 구현체로 쉽게 대체 가능
### Import 패키지 설명
#### `java.util.List`
- 용도: 여러 회원을 반환하는 컬렉션 타입
- 특징: 순서가 보장되는 컬렉션 인터페이스
- 사용 예: `findAll()` 메서드에서 모든 회원 목록 반환
#### `java.util.Optional<T>`
- 용도: null 값 처리를 안전하게 하기 위한 래퍼 클래스
- 특징:
    - Java 8부터 도입된 null 안전성 보장 메커니즘
    - 값이 있을 수도, 없을 수도 있음을 명시적으로 표현
- 사용 예:
    - `findById()`: 특정 ID의 회원이 없을 수 있음
    - `findByName()`: 특정 이름의 회원이 없을 수 있음
- 장점:
    - NullPointerException 방지
    - 명시적인 null 체크 가능
    - 함수형 프로그래밍 스타일 지원
## 4. 리포지토리 레이어: MemoryMemberRepository 구현체
### 파일 위치
```1:43:src/main/java/hello/hello_spring/repository/MemoryMemberRepository.java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//동시성 문제 해결 안 됨
//실무에서는 ConcurrentHashMap, AtomicLong 사용 고려
public class MemoryMemberRepository implements MemberRepository {
    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;
    
    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }
    
    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
    
    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
    
    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }
    
    public void clearStore() {
        store.clear();
    }
}
```
### 작동 원리
#### 데이터 저장 구조
- HashMap<Long, Member>: 회원 ID를 키로, 회원 객체를 값으로 저장
- sequence: 자동 증가하는 ID 생성기
#### 메서드별 동작
1. save():
    - 시퀀스를 증가시켜 ID 할당
    - HashMap에 저장
    - 저장된 객체 반환
2. findById():
    - HashMap의 get() 메서드로 즉시 조회 (O(1) 시간 복잡도)
    - Optional.ofNullable()로 null 안전 처리
3. findAll():
    - HashMap의 values()를 ArrayList로 변환하여 반환
    - 원본 데이터 보호를 위해 새로운 리스트 생성
4. findByName():
    - Java Stream API 사용
    - 필터링을 통해 이름이 일치하는 첫 번째 회원 반환
5. clearStore():
    - 테스트용 메서드
    - 각 테스트 간 데이터 격리
### 설계 이유
1. 개발 단계 적합성:
    - 데이터베이스 설정 없이 빠른 개발 및 테스트 가능
    - 외부 의존성 없이 순수 자바로 구현
2. 프로토타이핑:
    - 초기 개발 단계에서 비즈니스 로직 검증에 집중
    - 나중에 실제 DB로 쉽게 교체 가능
3. 테스트 편의성:
    - 빠른 테스트 실행
    - 테스트 격리 용이
### 주의사항
- 동시성 문제: HashMap과 일반 long은 스레드 안전하지 않음
- 실무 대안:
    - `ConcurrentHashMap`: 스레드 안전한 Map
    - `AtomicLong`: 스레드 안전한 시퀀스 생성
### Import 패키지 설명
#### `java.util.HashMap`
- 용도: 키-값 쌍으로 데이터를 저장하는 해시 테이블 기반 자료구조
- 특징:
    - O(1) 평균 시간 복잡도로 빠른 조회/삽입
    - null 키와 null 값 허용
    - 순서 보장 안 됨 (Java 8+ LinkedHashMap은 순서 보장)
- 단점: 스레드 안전하지 않음
#### `java.util.Map`
- 용도: HashMap의 인터페이스 타입으로 선언
- 이점: 구현체 교체 용이 (예: TreeMap, LinkedHashMap)
#### `java.util.ArrayList`
- 용도: 동적 배열로 리스트 구현
- 사용: HashMap의 values()를 리스트로 변환
- 특징: 순서 보장, 크기 조절 가능
#### `java.util.Optional`
- 사용: `Optional.ofNullable()`로 null 안전 처리
- 메서드:
    - `ofNullable()`: null이면 빈 Optional, 값이 있으면 값 포함 Optional 반환
#### Java Stream API
- 용도: 함수형 프로그래밍 스타일로 컬렉션 처리
- 메서드 체이닝:
    - `stream()`: 컬렉션을 스트림으로 변환
    - `filter()`: 조건에 맞는 요소만 필터링
    - `findAny()`: 조건에 맞는 임의의 요소 반환 (Optional)
## 5. 서비스 레이어: MemberService
### 파일 위치
```1:38:src/main/java/hello/hello_spring/service/MemberService.java
package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepository;
    
    //회원 서비스 코드를 DI 가능하도록
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    //회원 가입
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 검증
        memberRepository.save(member);
        return member.getId();
    }
    
    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원");
                });
    }
    
    //전체 회원 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```
### 작동 원리
#### 의존성 주입 (Dependency Injection)
- 생성자 주입: 생성자를 통해 MemberRepository를 주입받음
- final 필드: 한 번 주입되면 변경 불가능 (불변성 보장)
#### 비즈니스 로직
1. join(): 회원 가입
    - 중복 검증 → 저장 → ID 반환
2. validateDuplicateMember(): 중복 회원 검증
    - 이름으로 회원 조회
    - 존재하면 예외 발생
    - Optional의 `ifPresent()` 활용
3. findMembers(): 전체 회원 조회
    - 리포지토리의 findAll() 위임
4. findOne(): 단일 회원 조회
    - 리포지토리의 findById() 위임
### 설계 이유
#### 1. 계층 분리
- 서비스 계층의 책임: 비즈니스 로직 처리
- 리포지토리 계층의 책임: 데이터 접근만 담당
- 명확한 책임 분리: 각 계층이 자신의 역할만 수행
#### 2. 의존성 주입 (DI)
- 생성자 주입 방식:
    - 필수 의존성 명확히 표현
    - final로 불변성 보장
    - 테스트 시 Mock 객체 주입 용이
#### 3. 비즈니스 로직 중앙화
- 중복 검증 로직: 서비스 계층에서 처리
- 트랜잭션 관리: 나중에 Spring과 통합 시 트랜잭션 경계 설정 가능
- 보안 및 권한: 서비스 계층에서 처리 가능
#### 4. Optional 활용
- null 안전성: Optional을 통해 안전한 null 처리
- 함수형 스타일: `ifPresent()`로 간결한 코드
### Import 패키지 설명
#### `java.util.List`
- 용도: 여러 회원을 반환하는 컬렉션
- 사용: `findMembers()` 메서드 반환 타입
#### `java.util.Optional`
- 용도: null 안전한 단일 회원 반환
- 사용:
    - `findOne()`: 회원이 없을 수 있음을 명시
    - `validateDuplicateMember()`: `ifPresent()`로 중복 체크
## 6. 테스트 레이어: MemoryMemberRepositoryTest
### 파일 위치
```1:66:src/test/java/hello/hello_spring/repository/MemoryMemberRepositoryTest.java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class MemoryMemberRepositoryTest {
    MemoryMemberRepository repository = new MemoryMemberRepository();
    
    @AfterEach // 각 테스트 종료 때마다 실행
    public void afterEach() {
        repository.clearStore();
    }
    
    @Test // 테스트는 독립적 실행 (순서 의존x)
    public void save() {
        //given
        Member member = new Member();
        member.setName("spring");
        
        //when
        repository.save(member);
        
        //then
        Member result = repository.findById(member.getId()).get();
        assertThat(result).isEqualTo(member);
    }
    
    @Test
    public void findByName() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);
        
        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);
        
        //when
        Member result = repository.findByName("spring1").get();
        
        //then
        assertThat(result).isEqualTo(member1);
    }
    
    @Test
    public void findAll() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);
        
        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);
        
        //when
        List<Member> result = repository.findAll();
        
        //then
        assertThat(result.size()).isEqualTo(2);
    }
}
```
### 작동 원리
#### 테스트 라이프사이클
1. @AfterEach: 각 테스트 메서드 실행 후 실행
    - 데이터 초기화로 테스트 간 격리 보장
    - 테스트 간 의존성 제거
#### Given-When-Then 패턴
- Given: 테스트 데이터 준비
- When: 테스트 대상 메서드 실행
- Then: 결과 검증
### 설계 이유
#### 1. 테스트 격리
- @AfterEach: 각 테스트 후 데이터 초기화
- 독립성: 테스트 실행 순서와 무관하게 동작
#### 2. 검증 방식
- AssertJ: 유창한 API로 가독성 높은 검증
- 단정문: `assertThat().isEqualTo()`로 명확한 검증
#### 3. 테스트 커버리지
- save(): 저장 기능 검증
- findByName(): 이름으로 조회 검증
- findAll(): 전체 조회 검증
### Import 패키지 설명
#### `org.junit.jupiter.api.Test`
- 용도: JUnit 5의 테스트 메서드 표시 어노테이션
- 특징:
    - JUnit 5 (Jupiter)는 모던 자바 테스트 프레임워크
    - `@Test` 어노테이션이 붙은 메서드가 테스트로 실행됨
#### `org.junit.jupiter.api.AfterEach`
- 용도: 각 테스트 메서드 실행 후 정리 작업
- 사용: 테스트 데이터 초기화
#### `static org.assertj.core.api.Assertions.*`
- 용도: AssertJ 라이브러리의 정적 메서드 import
- 특징:
    - 유창한 API (Fluent API)
    - `assertThat(actual).isEqualTo(expected)` 형태
    - 가독성 높은 검증 코드
- 예시:
  ```java
  assertThat(result).isEqualTo(member);
  assertThat(result.size()).isEqualTo(2);
  ```
#### `java.util.List`
- 용도: `findAll()` 테스트에서 반환 타입 검증
## 7. 테스트 레이어: MemberServiceTest
### 파일 위치
```1:57:src/test/java/hello/hello_spring/service/MemberServiceTest.java
package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import hello.hello_spring.repository.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceTest {
    MemberService memberService;
    MemoryMemberRepository memberRepository;
    
    @BeforeEach
    public void beforeEach() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);
    }
    
    @AfterEach
    public void afterEach() {
        memberRepository.clearStore();
    }
    
    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("hello");
        
        //when
        Long saveId = memberService.join(member);
        
        //then
        Member findMember = memberRepository.findById(saveId).get();
        assertEquals(member.getName(), findMember.getName());
    }
    
    @Test
    public void 중복회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("spring");
        
        Member member2 = new Member();
        member2.setName("spring");
        
        //when
        memberService.join(member1);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> memberService.join(member2));
        
        //then
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원");
    }
}
```
### 작동 원리
#### 테스트 라이프사이클
1. @BeforeEach: 각 테스트 전에 실행
    - 새로운 리포지토리와 서비스 인스턴스 생성
    - 의존성 주입 시뮬레이션
2. @AfterEach: 각 테스트 후에 실행
    - 데이터 초기화
#### 테스트 케이스
1. 회원가입():
    - 정상적인 회원 가입 플로우 검증
    - 저장 후 조회하여 검증
2. 중복회원_예외():
    - 예외 처리 검증
    - `assertThrows()`로 예외 발생 확인
    - 예외 메시지 검증
### 설계 이유
#### 1. 의존성 주입 테스트
- @BeforeEach: 각 테스트마다 새로운 인스턴스 생성
- 동일한 리포지토리: 서비스와 테스트가 같은 리포지토리 인스턴스 공유
- 실제 동작 시뮬레이션: DI 패턴을 테스트 코드에서 구현
#### 2. 예외 처리 검증
- assertThrows(): 예외 발생 여부 검증
- 예외 메시지 검증: 예외의 정확성까지 검증
#### 3. 통합 테스트
- 서비스 + 리포지토리: 실제 비즈니스 로직과 데이터 접근 로직 통합 검증
- 엔드투엔드 검증: 저장부터 조회까지 전체 플로우 검증
### Import 패키지 설명
#### `org.junit.jupiter.api.BeforeEach`
- 용도: 각 테스트 메서드 실행 전 초기화 작업
- 사용: 테스트 객체 생성 및 의존성 설정
#### `org.junit.jupiter.api.AfterEach`
- 용도: 각 테스트 메서드 실행 후 정리 작업
- 사용: 테스트 데이터 초기화
#### `org.junit.jupiter.api.Test`
- 용도: 테스트 메서드 표시
#### `static org.assertj.core.api.Assertions.*`
- 용도: AssertJ의 검증 메서드
- 사용: `assertThat().isEqualTo()` 형태의 검증
#### `static org.junit.jupiter.api.Assertions.*`
- 용도: JUnit 5의 기본 검증 메서드
- 사용:
    - `assertEquals()`: 값 동등성 검증
    - `assertThrows()`: 예외 발생 검증
#### `java.lang.reflect.Member` (참고)
- 주의: 이 import는 실제로 사용되지 않음 (코드에서 제거됨)
- 실제 사용: `hello.hello_spring.domain.Member`
## 8. 전체 아키텍처 요약
### 의존성 흐름
```
컨트롤러 → 서비스 → 리포지토리 인터페이스 ← 리포지토리 구현체
              ↓
           도메인
```
### 핵심 설계 원칙
1. 계층 분리: 각 계층이 명확한 책임을 가짐
2. 의존성 역전: 인터페이스를 통한 추상화
3. 테스트 가능성: 각 계층을 독립적으로 테스트 가능
4. 확장성: 구현체 변경이 다른 계층에 영향 없음
### 패키지 구조
```
hello.hello_spring
├── domain          # 도메인 모델 (Entity)
│   └── Member.java
├── repository      # 데이터 접근 계층
│   ├── MemberRepository.java (인터페이스)
│   └── MemoryMemberRepository.java (구현체)
└── service         # 비즈니스 로직 계층
    └── MemberService.java
```
### Java 표준 라이브러리 활용
- java.util.List: 컬렉션 처리
- java.util.Optional: null 안전성
- java.util.HashMap: 빠른 데이터 저장/조회
- Java Stream API: 함수형 컬렉션 처리
### 테스트 프레임워크
- JUnit 5: 모던 자바 테스트 프레임워크
- AssertJ: 유창한 검증 API
## 9. 향후 개선 방향
### Spring 통합
- @Repository: 리포지토리 빈 등록
- @Service: 서비스 빈 등록
- @Autowired: 의존성 자동 주입
- @Transactional: 트랜잭션 관리
### 데이터베이스 연동
- JPA/Hibernate: 객체-관계 매핑
- Spring Data JPA: 리포지토리 자동 구현
- 실제 DB: MySQL, PostgreSQL 등
### 동시성 개선
- ConcurrentHashMap: 스레드 안전한 Map
- AtomicLong: 스레드 안전한 시퀀스
- 동기화 처리: 멀티스레드 환경 대응
1. 애플리케이션 개발 시 비즈니스 핵심 로직(예: 중복 회원 확인)을 주로 담당하는 계층은 무엇일까요?
   컨트롤러(Controller)
   [`서비스(Service)`]
   리포지토리(Repository)
   도메인(Domain)
   해설
   서비스 계층은 애플리케이션의 핵심 비즈니스 규칙을 구현합니다. 사용자의 요청을 처리하기 위해 리포지토리 등을 활용해요. 컨트롤러는 요청을 받고, 리포지토리는 데이터 접근을 담당합니다.
2. 데이터 저장 방식이 아직 결정되지 않은 상황에서, 리포지토리 구현체의 변경에 유연하게 대처하기 위한 설계 방식은 무엇인가요?
   구현체를 먼저 만들고 나중에 인터페이스를 추가한다.
   [`리포지토리 인터페이스를 정의하고, 임시 구현체(예: 메모리)를 사용한다.`]
   데이터베이스 기술이 확정될 때까지 개발을 중단한다.
   단일 클래스에 모든 기능을 구현한다.
   해설
   리포지토리를 인터페이스로 추상화하면, 실제 데이터 저장 기술(DB, JPA 등)이 나중에 결정되어도 인터페이스를 구현하는 다른 클래스로 쉽게 교체할 수 있습니다. 초기엔 메모리 구현체를 활용하기도 합니다.
3. JUnit 테스트 케이스 작성 시, 여러 테스트 메서드가 실행될 때 각 테스트가 서로 독립적으로 동작하게 만들기 위한 방법은 무엇인가요?
   모든 테스트 코드를 하나의 메서드에 작성한다.
   [`테스트 실행 후 공유 데이터를 정리(clear)하는 코드를 추가한다.`]
   테스트 메서드의 이름을 알파벳 순서로 정렬한다.
   테스트에서 사용하는 객체를 모두 Static으로 선언한다.
   해설
   테스트는 실행 순서와 관계없이 독립적이어야 합니다. 이전 테스트의 결과(공유 데이터)가 다음 테스트에 영향을 주지 않도록, 각 테스트 실행 전/후에 데이터를 정리하는 작업이 중요합니다.
4. 서비스 계층과 리포지토리 계층의 역할 및 이름 부여 방식에 대한 설명 중 적절하지 않은 것은 무엇일까요?
   서비스는 비즈니스 관련 용어를 사용한다.
   리포지토리는 데이터 저장 및 조회에 집중한다.
   [`서비스 계층이 복잡한 데이터 입출력 로직을 주로 담당한다.`]
   리포지토리는 서비스 계층에 의존하지 않는다.
   해설
   리포지토리는 데이터 접근(저장, 조회 등)에 집중하며, 서비스 계층은 리포지토리를 활용하여 비즈니스 로직을 수행합니다. 복잡한 비즈니스 로직은 서비스 계층의 역할입니다.
5. 서비스 객체가 자신이 의존하는 리포지토리 객체의 구현체를 직접 생성하지 않고, 외부(설정 등)에서 전달받아 사용하는 설계 방식을 무엇이라고 하나요?
   추상화 (Abstraction)
   [`의존성 주입 (Dependency Injection)`]
   캡슐화 (Encapsulation)
   상속 (Inheritance)
   해설
   의존성 주입(DI)은 객체가 필요한 다른 객체(의존성)를 직접 생성하지 않고 외부에서 받아 사용하는 방식입니다. 코드의 재사용성을 높이고 테스트하기 쉽게 만들어 줍니다.