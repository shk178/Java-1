에러의 핵심 원인은 **Java 버전과 Gradle(정확히는 Groovy)의 호환성 문제**입니다.

---

## 에러 핵심 해석

```
Unsupported class file major version 65
```

* **major version 65 = Java 21**
* 즉, **Java 21로 컴파일된 클래스**를
* **Gradle 7.5 (내부 Groovy)** 가 해석하려다 실패한 상황입니다.

에러 위치를 보면:

```
BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_'
```

→ `build.gradle`을 **Groovy 스크립트로 파싱하는 단계**에서 터졌습니다.
즉, 애플리케이션 코드 문제가 아니라 **빌드 도구 자체가 Java 21을 이해 못함**이라는 뜻입니다.

---

## 현재 조합 정리 (추정)

* 사용 중인 JDK: **Java 21**
* 사용 중인 Gradle Wrapper: **7.5**
* Gradle 7.5는 **Java 21 미지원**

Gradle ↔ Java 지원 관계는 꽤 엄격합니다.

---

## 해결 방법 2가지 (권장 순서)

### ✅ 방법 1: Gradle을 올린다 (가장 깔끔)

**Gradle 8.4 이상**부터 Java 21을 공식 지원합니다.

#### 1️⃣ 현재 Gradle Wrapper 버전 확인

```
gradlew -v
```

#### 2️⃣ Gradle Wrapper 업그레이드

프로젝트 루트에서:

```
gradlew wrapper --gradle-version 8.5
```

(8.4 이상이면 충분하지만 8.5 권장)

그러면 다음 파일이 자동 수정됩니다:

* `gradle/wrapper/gradle-wrapper.properties`

#### 3️⃣ 다시 실행

```
gradlew explodedWar
```

---

### ✅ 방법 2: Java 버전을 낮춘다 (임시 회피)

Gradle 7.5를 유지해야 한다면:

* **Java 17 또는 Java 11** 사용

#### 1️⃣ Java 17 설치

* Temurin / Oracle JDK 17

#### 2️⃣ JAVA_HOME 변경

Windows 기준 예시:

```
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
```

확인:

```
java -version
```

#### 3️⃣ 다시 실행

```
gradlew explodedWar
```

---

## 어떤 선택이 맞을까?

* **Spring Boot 최신 + Java 21 사용 중** → 👉 Gradle 업그레이드 (방법 1)
* **회사/레거시 프로젝트** → 👉 Java 17로 낮추기 (방법 2)

요즘 Spring Boot 3.x + Java 21 조합이면 **Gradle 8.x는 사실상 필수**입니다.

---

## 추가로 확인하면 좋은 것

1. `gradle-wrapper.properties` 안에:

   ```
   distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-bin.zip
   ```

   → 이게 7.5면 문제 확정

2. IDE에서 사용하는 JDK와
   터미널의 `java -version`이 같은지도 확인

---

원하면

* 현재 `java -version`
* `gradlew -v`
* `build.gradle` 상단

이 3개 올려주면, **정확히 어떤 조합이 꼬였는지** 딱 짚어줄게.
