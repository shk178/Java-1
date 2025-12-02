요청이 3가지 방식(@RequestParam / @PathVariable / @ModelAttribute)인데,
**스프링이 DTO를 어떻게 바인딩하는지에 따라 “정확하게 어떤 입력값을 보내야 하는지”가 전부 다르다.**

아래에 **각 방식별로 정확히 어떤 URL/파라미터를 보내야 정상적으로 One 객체가 만들어지는지** 정리해줄게.

---

# 0. 전제: DTO 구조

```java
public class One {
    private int oneInt;
    private Integer oneInteger;
    private String oneString;

    // getter/setter
}
```

그리고 **추가 Converter 없이**
기본적인 스프링 바인딩 방식만 사용할 것으로 가정한다.

---

# 1) @RequestParam One one → “필드별 쿼리 파라미터 방식”

```java
@GetMapping("/convert-1")
public One convert1(@RequestParam One one) {
    return one;
}
```

### 스프링 동작

스프링은 @RequestParam One 을 “하나의 DTO 전체”로 보지만
실제로는 **각 필드를 쿼리 파라미터에서 읽어온다.**

즉:

### 입력값 (정상 동작)

```
http://localhost:8080/convert-1?oneInt=1&oneInteger=10&oneString=ABC
```

이렇게 보내야 One 객체가 채워진다.

---

# 2) @PathVariable One one → “경로 변수에서 전체 DTO 채우기”

```java
@GetMapping("/convert-2/{One}")
public One convert2(@PathVariable One one) {
    return one;
}
```

주의할 점:

### @PathVariable One 는 “문자열 전체를 One으로 변환하기 위한 Converter<String, One>”이 필요함

스프링 기본 바인딩으로는 **PathVariable → DTO** 가 불가능하다.
왜냐면 PathVariable 은 단일 문자열이기 때문에
스프링은 다음 변환을 시도함:

```
String → One
```

그런데 기본적으로 **String → DTO 변환기는 없다.**

따라서
**Converter<String, One> 를 직접 등록하지 않은 상태에서는 이 방식은 동작하지 않는다.**

그럼에도 불구하고,
“입력값” 자체는 어떻게 생겼는지 알려달라는 요청이라면,
Converter<String, One> 을 가정하고 아래처럼 보낼 수 있다:

### Converter를 만든 경우 가능한 입력값 예시

```
http://localhost:8080/convert-2/oneInt:1,oneInteger:10,oneString:ABC
```

---

# 3) @ModelAttribute One one → “가장 자연스러운 DTO 바인딩 방식”

```java
@GetMapping("/convert-3")
public One convert3(@ModelAttribute One one) {
    return one;
}
```

### @ModelAttribute는 쿼리스트링 기반의 “필드 단위 바인딩”이 기본

따라서 다음과 같이 보내면 정상 동작한다:

```
http://localhost:8080/convert-3?oneInt=1&oneInteger=10&oneString=ABC
```

이것은 convert-1 과 입력 방식이 동일하다.

---

# 4) 요약표

| 메서드                             | 입력값 형식                        | 예시                                                | 별도 Converter 필요 여부              |
| ------------------------------- | ----------------------------- | ------------------------------------------------- | ------------------------------- |
| convert-1 (@RequestParam One)   | 쿼리 파라미터 (필드별)                 | `/convert-1?oneInt=1&oneInteger=10&oneString=ABC` | 필요 없음                           |
| convert-2 (@PathVariable One)   | PathVariable 하나를 전체 One 으로 변환 | `/convert-2/oneInt:1,oneInteger:10,oneString:ABC` | **필요함(Converter<String, One>)** |
| convert-3 (@ModelAttribute One) | 쿼리 파라미터 (필드별)                 | `/convert-3?oneInt=1&oneInteger=10&oneString=ABC` | 필요 없음                           |

---

# 5) 결론

### Converter 없이 "바로" 테스트 가능한 방법은 convert-1, convert-3 뿐이다.

둘 다 다음 입력값으로 정상 작동한다.

```
oneInt=1&oneInteger=10&oneString=ABC
```

즉:

```
http://localhost:8080/convert-1?oneInt=1&oneInteger=10&oneString=ABC
http://localhost:8080/convert-3?oneInt=1&oneInteger=10&oneString=ABC
```

이 두 개는 Converter 없이 바로 된다.

반면 convert-2 는
String → One 변환기가 없으면 절대 동작하지 않는다.
이건 스프링 MVC 구조 때문에 어쩔 수 없다.

---

원하면 `Converter<String, One>` 하나를 만들어서
convert-2 까지도 정상 동작하는 전체 예제를 만들어줄게.

아래는 **Converter<String, One> 를 만든 경우**,
convert-1 / convert-2 / convert-3 각각에서 어떤 입력값이 동작하는지
정확히 설명해줄게.

이제 convert-2 (@PathVariable One one) 도 정상 동작하게 만들 수 있어.

---

# 0. 전제: DTO One

```java
public class One {
    private int oneInt;
    private Integer oneInteger;
    private String oneString;

    // getter / setter
}
```

---

# 1. Converter<String, One> 구현

아래는 예시 포맷을 `"oneInt:1,oneInteger:10,oneString:ABC"` 로 가정한 Converter다.

```java
public class StringToOneConverter implements Converter<String, One> {

    @Override
    public One convert(String source) {

        One one = new One();

        String[] parts = source.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length != 2) {
                throw new IllegalArgumentException("잘못된 포맷: " + part);
            }

            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "oneInt" -> one.setOneInt(Integer.parseInt(value));
                case "oneInteger" -> one.setOneInteger(Integer.parseInt(value));
                case "oneString" -> one.setOneString(value);
                default -> throw new IllegalArgumentException("알 수 없는 key: " + key);
            }
        }

        return one;
    }
}
```

---

# 2. Converter 등록

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToOneConverter());
    }
}
```

이제 스프링은 다음 변환이 가능해진다:

```
String → One
```

그리고 아래 방식에서 자동 적용된다:

* @RequestParam One one
* @PathVariable One one

(단, @ModelAttribute는 별도로 설명)

---

# 3. 컨트롤러 코드

```java
@RestController
public class ConvertController {

    @GetMapping("/convert-1")
    public One convert1(@RequestParam One one) {
        return one;
    }

    @GetMapping("/convert-2/{one}")
    public One convert2(@PathVariable One one) {
        return one;
    }

    @GetMapping("/convert-3")
    public One convert3(@ModelAttribute One one) {
        return one;
    }
}
```

주의:
세 번째 메서드는 `@ModelAttribute` 동작 방식이 다르기 때문에 결과가 달라짐.

---

# 4. 이제 어떤 입력값을 보내야 하는가

## convert-1: @RequestParam One one

**문자열 전체를 One으로 해석할 수 있게 되므로,
쿼리 파라미터로 “String → One 포맷”을 넣으면 된다.**

예시:

```
http://localhost:8080/convert-1?one=oneInt:1,oneInteger:10,oneString:ABC
```

이때 Converter<String, One> 이 실행되어 One으로 변환된다.

### 기존의 필드별 방식도 동작할까?

아니.
현재는 스프링이 “필드별 바인딩”이 아니라 “String → One 직접 변환”을 먼저 시도한다.

즉, 다음은 **더 이상 동작하지 않는다**:

```
?oneInt=1&oneInteger=10&oneString=ABC
```

왜냐면 @RequestParam One one 은 **단일 값(one)** 를 기대하기 때문이다.

---

## convert-2: @PathVariable One one

이제 정상적으로 동작한다.

예:

```
http://localhost:8080/convert-2/oneInt:1,oneInteger:10,oneString:ABC
```

경로의 전체 문자열이 Converter<String, One> 에 의해 One으로 변환된다.

---

## convert-3: @ModelAttribute One one

여기는 매우 중요하다:

### @ModelAttribute 는 Converter<String, One> 를 사용하지 않는다.

이 방식은 **기본적으로 필드 단위 바인딩**이다.

즉:

```
/convert-3?oneInt=1&oneInteger=10&oneString=ABC
```

이런 방식만 동작한다.

반면 다음은 동작하지 않는다:

```
/convert-3?one=oneInt:1,oneInteger:10,oneString:ABC   // 동작 안 함
/convert-3/oneInt:1,oneInteger:10,oneString:ABC       // 동작 안 함
```

왜?
@ModelAttribute 는 다음 순서로 동작하기 때문이다:

1. new One()
2. 쿼리스트링에서 필드명과 일치하는 파라미터 경우 setter 호출 (oneInt, oneString 등)
3. 절대 “전체 문자열 → 객체 변환” 로직을 사용하지 않음

즉, @ModelAttribute 는 **Converter<String, One> 를 무시**한다.

---

# 5. 최종 요약

| 메서드                             | 입력 방식                     | Converter<String, One> 사용 여부 | 예시 URL                                                |
| ------------------------------- | ------------------------- | ---------------------------- | ----------------------------------------------------- |
| convert-1 (@RequestParam One)   | 문자열 전체를 One으로 변환          | **사용함**                      | `/convert-1?one=oneInt:1,oneInteger:10,oneString:ABC` |
| convert-2 (@PathVariable One)   | PathVariable 전체를 One으로 변환 | **사용함**                      | `/convert-2/oneInt:1,oneInteger:10,oneString:ABC`     |
| convert-3 (@ModelAttribute One) | 필드별 쿼리 파라미터 바인딩           | **사용하지 않음**                  | `/convert-3?oneInt=1&oneInteger=10&oneString=ABC`     |

즉,

* @RequestParam One → “String → One Converter” 사용
* @PathVariable One → “String → One Converter” 사용
* @ModelAttribute One → 절대 사용하지 않음 → 필드 바인딩 방식

---

원하면
컨버터 두 개(String → One, One → Two)를 조합해서
완전한 체인 변환도 다시 정리해줄 수 있어.
