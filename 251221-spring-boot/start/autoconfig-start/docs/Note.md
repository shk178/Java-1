문제의 핵심은 **`MemoryController`가 Spring MVC 컨트롤러로 인식되지 않고 있다**는 점입니다.
그래서 **테스트에서 빈 주입은 되지만, HTTP 요청 → `@GetMapping`은 동작하지 않는** 상황입니다.

아래에서 왜 이런 일이 발생하는지와 해결 방법을 단계별로 정리할게요.

---

## 1. 가장 치명적인 원인: `@RestController` / `@Controller`가 아님

현재 클래스 선언은 다음과 같습니다.

```java
@ResponseBody
@RequiredArgsConstructor
public class MemoryController {
```

### 문제점

* `@ResponseBody`는 **메서드 또는 컨트롤러에 붙어 응답 방식만 지정**할 뿐
* **이 클래스가 컨트롤러라는 사실을 Spring에 등록하지 않습니다**

즉,

* Bean으로는 존재함
* 하지만 **HandlerMapping 대상이 아님**
* 그래서 `/memory` 요청이 매핑되지 않음

### 해결

반드시 아래 중 하나를 사용해야 합니다.

```java
@RestController
@RequiredArgsConstructor
public class MemoryController {
```

또는

```java
@Controller
@ResponseBody
@RequiredArgsConstructor
public class MemoryController {
```

`@RestController = @Controller + @ResponseBody` 이므로 보통 첫 번째를 사용합니다.

---

## 2. `@Configuration + @Bean`으로 컨트롤러를 직접 생성한 구조의 문제

```java
@Configuration
@Conditional(MemoryCondition.class)
public class MemoryConfig {

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }
}
```

이 방식 자체는 **Bean 생성에는 문제 없음**
하지만 **Spring MVC는 다음을 기대합니다**

* `@Controller` / `@RestController` 애노테이션
* `RequestMappingHandlerMapping`이 스캔 가능해야 함

컨트롤러를 `@Bean`으로 직접 등록할 경우에도
**반드시 `@RestController`가 있어야 URL 매핑 대상이 됩니다**

---

## 3. 그래서 테스트에서는 왜 주입이 되나?

테스트에서 이런 건 잘 될 겁니다.

```java
@Autowired
MemoryController memoryController;
```

이유:

* `@Conditional` 조건이 true
* `@Configuration`에서 `@Bean`으로 등록됨
* **단순 Bean으로는 정상 주입**

하지만:

```http
GET /memory
```

는 실패
→ **HandlerMapping에 등록되지 않았기 때문**

---

## 4. 정리: 현재 상태를 한 문장으로 설명하면

> MemoryController는 Spring Bean이긴 하지만
> Spring MVC가 인식하는 컨트롤러는 아니다

---

## 5. 최소 수정으로 해결하는 정답 코드

```java
@RestController
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryFinder memoryFinder;

    @GetMapping("/memory")
    public Memory system() {
        System.out.println("컨트롤러 실행됨");
        System.out.println(memoryFinder.get());
        return memoryFinder.get();
    }
}
```

```java
@Conditional(MemoryCondition.class)
@Configuration
public class MemoryConfig {

    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}
```

※ 컨트롤러는 **직접 @Bean으로 만들지 않는 것이 정석**입니다.

---

## 6. 권장 구조 (실무 기준)

* 조건부 제어는 **Service / Component 단에서**
* Controller는 항상 MVC에 노출

```java
@RestController
@RequiredArgsConstructor
public class MemoryController {
    private final Optional<MemoryFinder> memoryFinder;

    @GetMapping("/memory")
    public Memory system() {
        return memoryFinder
                .map(MemoryFinder::get)
                .orElse(null);
    }
}
```

---

원하시면

* `@WebMvcTest`에서 왜 더 안 되는지
* `@ConditionalOnProperty`로 더 깔끔하게 바꾸는 방법
* 테스트에서 조건별로 컨트롤러 활성/비활성 검증하는 법

도 이어서 설명해 드릴게요.
