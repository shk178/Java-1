스프링에서 **HTTP 세션 타임아웃(만료 시간)**을 설정하는 방법은 크게 **3가지**가 있습니다. 스프링 부트인지, 서블릿 컨테이너 설정을 쓸지, 자바 코드로 할지에 따라 선택하면 됩니다.

아래에서 가장 자주 쓰는 순서대로 정리해드릴게요.

---

# 1. `application.properties` 또는 `application.yml`에서 설정 (스프링 부트 권장)

스프링 부트는 **서블릿 컨테이너 세션 타임아웃을 설정하는 전용 설정**을 제공합니다.

### `application.properties`

```properties
server.servlet.session.timeout=30m
```

### `application.yml`

```yaml
server:
  servlet:
    session:
      timeout: 30m
```

**30분**으로 설정한 예시입니다.

지원되는 단위

* `s` (초)
* `m` (분)
* `h` (시간)

---

# 2. 자바 코드에서 설정 (`@Bean` 등록)

Spring Boot 2.x 이상에서는 `ServletContextInitializer`를 사용하여 세션 관련 설정을 직접 등록할 수 있습니다.

```java
@Bean
public ServletContextInitializer servletContextInitializer() {
    return servletContext -> {
        // 30분
        servletContext.setSessionTimeout(30);
    };
}
```

이 방식은 application.yml보다 우선순위가 높습니다.

---

# 3. `web.xml` 사용 (스프링 부트 아닌 WAR 배포)

스프링 부트가 아닌 경우 일반적인 서블릿 설정 방식입니다.

```xml
<session-config>
    <session-timeout>30</session-timeout> <!-- 분 단위 -->
</session-config>
```

---

# 4. 특정 세션만 개별로 타임아웃 설정하기

세션 전체가 아닌 **특정 사용자 세션만 별도**로 설정하고 싶을 때:

```java
HttpSession session = request.getSession();
session.setMaxInactiveInterval(1800); // 초 단위, 30분
```

예: 로그인 성공 시 사용자 세션만 따로 변경

```java
session.setMaxInactiveInterval(60 * 60); // 1시간
```

---

# 요약

| 방식                | 설명                     |
| ----------------- | ---------------------- |
| `application.yml` | 가장 간단하고 스프링 부트 공식 방식   |
| 자바 코드 Bean        | 특정 조건 기반 설정 필요할 때      |
| web.xml           | 서블릿 기반 WAR 배포 시        |
| 세션별 설정            | 로그인 등 개별 세션에만 설정 적용 가능 |

---

원하시면 **Spring Security remember-me 만료보다 세션 타임아웃이 짧아서 로그인이 풀리는 문제**, 또는 **세션 만료 후 redirect 처리** 같은 부분도 예제로 설명해 드릴 수 있습니다.
