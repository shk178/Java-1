# 7. 날짜와 시간
- 1. 지구가 태양을 한 바퀴 도는 평균 시간 365.xx일
- 그레고리력 (대부분 사용하는 달력): 1년 = 365일
- 윤년(leap year): 4년마다 2월 29일까지로 하루 추가한다.
- 100년 단위: 윤년x (1900, 2100년 윤년x)
- 400년 단위: 윤년o (2000년 윤년o)
- 2. 일광 절약 시간(Daylight Saving Time, DST)
- 3~10월에는 태양이 일찍 뜨고, 11~2월에는 늦게 뜬다.
- 국가나 지역에 따라 일광 절약 시간제 (썸머 타임) 적용한다.
- 3월 말 ~ 10월 말에 1시간 앞당긴다.
- 시계 변경:
- 서머타임이 시작되는 시점에는 시계 바늘을 1시간 앞으로 돌려 맞춥니다.
- 서머타임이 종료되면 시계 바늘을 1시간 뒤로 돌려 원래대로 되돌립니다.
- 시작 시: 오전 2시에서 3시로 시간을 앞당깁니다.
- 종료 시: 오전 2시에서 1시로 시간을 늦춥니다.
- 3. 타임존 계산
- UTC = 변하지 않는 기준 시간
- GMT = 원래는 런던 그리니치 천문대의 평균 태양시, UTC±0
- Europe/London = UTC+0, 여름에는 UTC+1
- America/New_York = UTC-5, 여름에는 UTC-4
- Asia/Seoul = UTC+9
- Europe/Paris = UTC+1, 여름에는 UTC+2
- Europe/Berlin = UTC+1, 여름에는 UTC+2
- 서울에서 오후 9시에 미팅하려면 베를린에서는 몇 시일까?
- 답: 오후 1시거나 2시
- 4. 자바의 날짜와 시간 라이브러리
- java.util.Date -> java.util.Calendar -> java.time 패키지
- 주요 날짜/시간 클래스
- (1) LocalDate (날짜만)
- 설명: 시간대 없는 날짜 (년-월-일)
- toString 출력: 2025-10-02
- (2) LocalTime (시간만)
- 설명: 시간대 없는 시간 (시:분:초.나노초)
- toString 출력: 14:30:15.123456789 또는 14:30:15
- (3) LocalDateTime (날짜+시간)
- 설명: 시간대 없는 날짜와 시간
- toString 출력: 2025-10-02T14:30:15.123456789
//Local이 붙으면 타임존이 적용되지 않는다.
- (4) ZonedDateTime (시간대 포함)
- 설명: 시간대가 포함된 날짜와 시간
- toString 출력: 2025-10-02T14:30:15.123+09:00[Asia/Seoul]
//타임존이 출력된다. 일광 절약 시간제가 적용된다.
- (5) OffsetDateTime (오프셋 포함)
- 설명: UTC 오프셋이 있는 날짜와 시간
- toString 출력: 2025-10-02T14:30:15.123+09:00
- (6) OffsetTime (오프셋 포함 시간)
- 설명: UTC 오프셋이 있는 시간
- toString 출력: 14:30:15.123+09:00
//타임존을 알 수 없어 일광 절약 시간제가 적용 안 된다.
- (7) Instant (타임스탬프)
- 설명: UTC 기준의 특정 시점 (에포크로부터의 나노초)
- toString 출력: 2025-10-02T05:30:15.123Z
//long seconds: Epoch로부터의 초
//int nanos: 초 이하의 나노초 (10억분의 1초)
//Unix Epoch = Unix Time = 1970년 1월 1일 00:00:00 UTC
- 기간/간격 클래스
- (1) Duration (시간 기반 간격)
- 설명: 초와 나노초 단위의 시간 간격
- toString 출력: PT8H30M15S (8시간 30분 15초)
- (2) Period (날짜 기반 간격)
- 설명: 년, 월, 일 단위의 기간
- toString 출력: P1Y2M3D (1년 2개월 3일)
//시간의 간격(기간)은 영어로 amount of time이다.
- 시간대/오프셋 관련
- (1) ZoneId (시간대 ID)
- 설명: 시간대 식별자
- toString 출력: Asia/Seoul 또는 UTC
- (2) ZoneOffset (UTC 오프셋)
- 설명: UTC로부터의 시간 차이
- toString 출력: +09:00 또는 Z (UTC의 경우)
- 날짜 구성요소 (Enum)
- (1) Month (월)
- 값: JANUARY, FEBRUARY, ..., DECEMBER
- toString 출력: JANUARY, FEBRUARY 등
- (2) DayOfWeek (요일)
- 값: MONDAY, TUESDAY, ..., SUNDAY
- toString 출력: MONDAY, TUESDAY 등
- (3) MonthDay (월-일)
- 설명: 월과 일만 (생일 등에 사용)
- toString 출력: --12-25 (12월 25일)
- (4) YearMonth (년-월)
- 설명: 년과 월만
- toString 출력: 2025-10 (2025년 10월)
- (5) Year (년)
- 설명: 년도만
- toString 출력: 2025
- 기타 유용한 클래스
- (1) Clock (시계)
- 설명: 현재 시간을 제공하는 추상화
- toString 출력: 구현체에 따라 다름
- 모든 toString 출력은 ISO-8601 형식 (국제 표준 날짜/시간 표기법)을 따른다.
- T는 날짜와 시간의 구분자, Z는 UTC를 의미한다.
- 5. 메서드
- (1) isBefore()
- 특정 날짜/시간이 다른 날짜/시간보다 이전인지 확인
- (2) isAfter()
- 특정 날짜/시간이 다른 날짜/시간보다 이후인지 확인
```java
LocalDate d1 = LocalDate.of(2025, 1, 1);
LocalDate d2 = LocalDate.of(2025, 2, 1);
System.out.println(d1.isBefore(d2)); //true
System.out.println(d2.isBefore(d1)); //false
System.out.println(d1.isAfter(d2)); //false
System.out.println(d2.isAfter(d1)); //true
```
- (3) isEqual()
- 날짜/시간 값이 동일한지 비교
- 타입 불일치면 컴파일 에러
- (4) equals()
- Object.equals()를 오버라이드 → 타입까지 같은지 비교
```java
import java.time.*;
public class Main2 {
    public static void main(String[] args) {
        LocalDate d1 = LocalDate.of(2025, 10, 2);
        LocalDate d2 = LocalDate.of(2025, 10, 2);
        System.out.println(d1.equals(d2)); //true
        System.out.println(d1.isEqual(d2)); //true
        ZonedDateTime z1 = ZonedDateTime.of(2025, 10, 2, 0, 0, 0, 0, ZoneId.of("Asia/Seoul"));
        ZonedDateTime z2 = ZonedDateTime.of(2025, 10, 1, 15, 0, 0, 0, ZoneId.of("UTC"));
        System.out.println(z1.equals(z2)); //false (타임존까지 달라서 객체 값이 다름)
        System.out.println(z1.isEqual(z2)); //true (실제로 같은 순간을 가리킴)
    }
}
```
- 6. 250929-java-mid1/src/time/DSTChecker.md
- 7. Instant: DB 정보 저장이나 다른 시스템과 정보 교환할 때 사용
- 항상 기준점 동일하므로 데이터 일관성 유지된다.
- 8. Temporal/TemporalAccessor 인터페이스
- TemporalAccessor: 읽기 전용 접근 인터페이스 (날짜/시간 필드 조회만 가능)
- ex) get(ChronoField), isSupported(TemporalField)
- Temporal extends TemporalAccessor: 쓰기 가능 인터페이스 (시간 연산 가능)
- ex) plus(), minus(), with()
- Temporal은 TemporalAccessor의 확장 인터페이스
//자바에서 클래스 → 인터페이스 관계: implements
//인터페이스 → 인터페이스 관계: extends
//implements는 클래스가 인터페이스를 구현할 때만 사용 가능
- Temporal을 구현하는 클래스는 필드 접근뿐 아니라 더하기/빼기 연산도 가능
- LocalDateTime implements Temporal, ChronoLocalDateTime<LocalDate>
- ZonedDateTime implements Temporal, ChronoZonedDateTime<LocalDate>
- Instant implements Temporal
- LocalDate, LocalTime 등도Temporal 구현체
- 9. TemporalAmount 인터페이스
- TemporalAmount: 시간의 양(기간)을 표현하는 인터페이스
- plusInto(Temporal), minusFrom(Temporal) 같은 메서드 포함
- Period 클래스가 구현 → 사람 친화적 기간 (년/월/일 단위)
- Duration 클래스가 구현 → 기계 친화적 기간 (초/나노초 단위)
- 10. TemporalUnit 인터페이스
- 시간의 단위를 표현하는 인터페이스 ("얼마나"의 단위를 나타냄)
- 메서드 예시
```
Duration getDuration() : 이 단위가 나타내는 시간 길이 (예: DAYS → 24시간, HOURS → 1시간)
boolean isDurationEstimated() : 이 단위가 고정된 길이인지(초/분/시간) 아니면 추정치인지(월/년 → 윤년, 월 길이 다름)
boolean isSupportedBy(Temporal temporal) : 특정 Temporal이 이 단위를 지원하는지
```
- 대표 구현체: ChronoUnit (열거형 enum)
```
Nanos, Micros, Millis, Seconds, Minutes, Hours, HalfDays,
Days, Weeks, Months, Years, Decades, Centuries, Millennia, Eras, Forever
```
- 11. TemporalField 인터페이스
- 시간의 필드를 표현하는 인터페이스 ("어떤 값"을 뽑아낼지를 나타냄. 단위가 아니라 "자리")
- 메서드 예시
```
boolean isSupportedBy(TemporalAccessor temporal) : 특정 Temporal에서 지원되는 필드인지
ValueRange range() : 필드의 값 범위 (예: 월 = 1~12)
long getFrom(TemporalAccessor temporal) : 해당 필드 값 가져오기
```
- 대표 구현체: ChronoField (열거형 enum)
```
NanoOfSecond, NanoOfDay, MicroOfSecond, MicroOfDay, MilliOfSecond, MilliOfDay, SecondOfMinute, SecondOfDay,
MinuteOfHour, MinuteOfDay, HourOfAmPm, ClockHourOfAmPm, HourOfDay, ClockHourOfDay, AmPmOfDay,
DayOfWeek, AlignedDayOfWeekInMonth, AlignedDayOfWeekInYear, DayOfMonth, DayOfYear,
EpochDay, AlignedWeekOfMonth, AlignedWeekOfYear, MonthOfYear, ProlepticMonth,
YearOfEra, Year, Era, InstantSeconds, OffsetSeconds
```
- 12. TemporalAdjusters는 정적 팩토리 메서드 모음 클래스
- TemporalAdjusters는 java.time.temporal에 있는 유틸리티 클래스
- 날짜(LocalDate, LocalDateTime 등 Temporal 구현체)를 특정 규칙에 따라 조정할 때 사용
- 반환값은 TemporalAdjuster (함수형 인터페이스)
- with(TemporalAdjuster)를 사용해서 날짜를 원하는 형태로 보정할 수 있다.
- 주요 메서드들
- (1) 요일 관련
```
next(DayOfWeek dayOfWeek) → 다음 해당 요일
nextOrSame(DayOfWeek dayOfWeek) → 다음 해당 요일, 오늘이 그 요일이면 오늘
previous(DayOfWeek dayOfWeek) → 이전 해당 요일
previousOrSame(DayOfWeek dayOfWeek) → 이전 해당 요일, 오늘이 그 요일이면 오늘
```
```java
LocalDate date = LocalDate.of(2025, 10, 2); //목요일
System.out.println(date.with(TemporalAdjusters.next(DayOfWeek.MONDAY))); //2025-10-06
System.out.println(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY))); //2025-10-02
```
- (2) 달 관련
```
firstDayOfMonth() → 이번 달 1일
lastDayOfMonth() → 이번 달 마지막 날
firstInMonth(DayOfWeek dayOfWeek) → 이번 달 첫 번째 해당 요일
lastInMonth(DayOfWeek dayOfWeek) → 이번 달 마지막 해당 요일
```
```java
LocalDate date = LocalDate.of(2025, 10, 2);
System.out.println(date.with(TemporalAdjusters.firstDayOfMonth()));  //2025-10-01
System.out.println(date.with(TemporalAdjusters.lastDayOfMonth()));   //2025-10-31
System.out.println(date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))); //2025-10-06
```
- (3) 년 관련
```
firstDayOfNextMonth() → 다음 달 1일
firstDayOfNextYear() → 다음 해 1월 1일
firstDayOfYear() → 이번 해 1월 1일
lastDayOfYear() → 이번 해 12월 31일
```
```java
LocalDate date = LocalDate.of(2025, 10, 2);
System.out.println(date.with(TemporalAdjusters.firstDayOfYear()));   //2025-01-01
System.out.println(date.with(TemporalAdjusters.lastDayOfYear()));    //2025-12-31
System.out.println(date.with(TemporalAdjusters.firstDayOfNextMonth())); //2025-11-01
```
- (4) TemporalAdjuster는 함수형 인터페이스라서 람다로 직접 메서드 구현할 수 있다.
```java
//오늘로부터 10일 뒤
LocalDate today = LocalDate.now();
LocalDate plus10 = today.with(t -> t.plus(10, ChronoUnit.DAYS));
System.out.println(plus10);
```
- 13. TemporalAdjuster 인터페이스
```
TemporalAdjusters (복수형, 끝에 's')
└─→ 클래스 (유틸리티)
    └─→ 정적 메서드들이 모여있음
        └─→ 예: firstDayOfMonth(), lastDayOfYear()
TemporalAdjuster (단수형, 's' 없음)
└─→ 인터페이스 (함수형)
    └─→ adjustInto(Temporal) 메서드 1개
        └─→ "날짜 조정 규칙"을 나타냄
```
```
TemporalAdjusters.firstDayOfMonth()
        ↓ 반환
TemporalAdjuster 객체 (조정 규칙)
        ↓ 전달
date.with(adjuster)
        ↓ 내부적으로 호출
adjuster.adjustInto(date)
        ↓ 반환
새로운 LocalDate 객체
```
```
TemporalAdjusters (복수형)
├─ 클래스 (final)
├─ 구현체 없음
└─ static 메서드만 제공
TemporalAdjuster (단수형)
├─ 인터페이스
├─ 구현체 있음
│   ├─ Java 내부 구현체들 (TemporalAdjusters가 반환)
│   ├─ 람다 표현식
│   └─ 커스텀 클래스
└─ adjustInto(Temporal) 메서드 1개
```
```java
LocalDate date = LocalDate.of(2025, 10, 15);
//1단계: TemporalAdjuster 생성
TemporalAdjuster adjuster = TemporalAdjusters.lastDayOfMonth();
//                          ^^^^^^^^^^^^^^^^^ 클래스 (복수형)
//                                            반환 → TemporalAdjuster (단수형)
//2단계: with()로 조정
LocalDate result = date.with(adjuster);
//                      ^^^^ with는 adjuster.adjustInto(date)를 호출
//한 줄로 쓰면
LocalDate result = date.with(TemporalAdjusters.lastDayOfMonth());
```
```java
LocalDate original = LocalDate.of(2025, 10, 15);
LocalDate adjusted = original.with(TemporalAdjusters.lastDayOfMonth());
System.out.println(original);  //2025-10-15 (변하지 않음)
System.out.println(adjusted);  //2025-10-31 (조정된 새 객체를 반환: 불변성)
```
- 14. TemporalAdjusters = "조정 도구 상자" (여러 도구가 들어있는 상자)
```
- 불변과는 무관한 개념
- 정적 메서드들을 모아놓은 유틸리티 클래스
- 인스턴스를 만들 수 없음 (생성자가 private)
- 상태(필드)가 없음
```
- TemporalAdjuster  = "특정 조정 도구" (예: "월말로 이동하는 도구")
```
- 자체는 불변과 무관 (인터페이스)
- 하지만 이것을 사용하는 방식이 불변성과 연결됨
```
- 불변인 것은 LocalDate, LocalDateTime 같은 날짜/시간 객체들
- TemporalAdjuster를 사용할 때 원본이 불변인 건 LocalDate 같은 객체들이 불변으로 설계되어서다.
```
//LocalDate가 불변이므로 모두 새 객체를 반환
date.with(adjuster);  //원본 date는 변하지 않음
date.plusDays(5);     //원본 date는 변하지 않음
date.minusMonths(2);  //원본 date는 변하지 않음
```
- 15. TemporalAdjuster 구현체
```
(1) Java 내부 구현체 (java.time.temporal.TemporalAdjusters 내부)
- TemporalAdjusters.firstDayOfMonth() → DayOfMonth 클래스
- TemporalAdjusters.lastDayOfMonth() → DayOfMonth 클래스
- TemporalAdjusters.firstDayOfYear() → DayOfYear 클래스
- TemporalAdjusters.lastDayOfYear() → DayOfYear 클래스
- TemporalAdjusters.next(DayOfWeek) → RelativeDayOfWeek 클래스
- TemporalAdjusters.previous(DayOfWeek) → RelativeDayOfWeek 클래스
- TemporalAdjusters.nextOrSame(DayOfWeek) → RelativeDayOfWeek 클래스
- TemporalAdjusters.previousOrSame(DayOfWeek) → RelativeDayOfWeek 클래스
- TemporalAdjusters.firstInMonth(DayOfWeek) → DayOfWeekInMonth 클래스
- TemporalAdjusters.lastInMonth(DayOfWeek) → DayOfWeekInMonth 클래스
- TemporalAdjusters.dayOfWeekInMonth(int, DayOfWeek) → DayOfWeekInMonth 클래스
- 이들은 모두 TemporalAdjusters 클래스 내부의 private static 클래스들
(2) ChronoField (enum)
- YEAR, MONTH_OF_YEAR, DAY_OF_MONTH 등
- TemporalField와 TemporalAdjuster를 모두 구현
(3) DayOfWeek (enum)
- MONDAY ~ SUNDAY
- TemporalAccessor, TemporalAdjuster를 구현
- ordinal()은 0~6, getValue()는 1~7
(4) 사용자 정의 구현체
- 람다 표현식
- 익명 클래스
- 커스텀 클래스
```
- 16. 파싱과 포맷팅
- DateTimeFormatter를 중심으로 이루어진다.
- 날짜/시간 객체: 불변이고 스레드 안전하다.
- (1) 기본 파싱
- String -> 날짜/시간 객체
```java
//기본 ISO 형식 파싱
LocalDate date = LocalDate.parse("2025-10-02");
LocalTime time = LocalTime.parse("14:30:15");
LocalDateTime dateTime = LocalDateTime.parse("2025-10-02T14:30:15");
ZonedDateTime zonedDateTime = ZonedDateTime.parse("2025-10-02T14:30:15+09:00[Asia/Seoul]");
```
- (2) 커스텀 패턴으로 파싱
```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
LocalDateTime dateTime = LocalDateTime.parse("2025/10/02 14:30", formatter);
//한글 포함 패턴
DateTimeFormatter koreanFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
LocalDate date = LocalDate.parse("2025년 10월 02일", koreanFormatter);
```
- (3) 기본 포맷팅
- 날짜/시간 객체 -> String
```java
LocalDateTime now = LocalDateTime.now();
//toString() - ISO 형식
String isoFormat = now.toString(); //"2025-10-02T14:30:15.123"
//미리 정의된 포맷터 사용
String formatted = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
```
- (4) 커스텀 패턴으로 포맷팅
```java
LocalDateTime dateTime = LocalDateTime.now();
//다양한 패턴 예시
DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String result1 = dateTime.format(formatter1); //"2025-10-02 14:30:15"
DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd a hh:mm");
String result2 = dateTime.format(formatter2); //"2025/10/02 오후 02:30"
DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일");
String result3 = dateTime.format(formatter3); //"2025년 10월 02일 목요일"
```
- (5) Locale 지정
```java
DateTimeFormatter formatter = DateTimeFormatter
    .ofPattern("yyyy년 MMM d일 E요일")
    .withLocale(Locale.KOREAN);
LocalDate date = LocalDate.of(2025, 10, 2);
String result = date.format(formatter); //"2025년 10월 2일 목요일"
```
- (6) 미리 정의된 포맷터
```java
//ISO 포맷터들
DateTimeFormatter.ISO_LOCAL_DATE        //"2025-10-02"
DateTimeFormatter.ISO_LOCAL_TIME        //"14:30:15.123"
DateTimeFormatter.ISO_LOCAL_DATE_TIME   //"2025-10-02T14:30:15.123"
DateTimeFormatter.ISO_ZONED_DATE_TIME   //"2025-10-02T14:30:15.123+09:00[Asia/Seoul]"
//기본 스타일
DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)    //"2025년 10월 2일 목요일"
DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)    //"2025년 10월 2일"
DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)  //"2025. 10. 2."
DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)   //"25. 10. 2."
```
- (7) 파싱 시 에러 처리
```java
try {
    LocalDate date = LocalDate.parse("2025-13-01"); //잘못된 월
} catch (DateTimeParseException e) {
    System.out.println("파싱 실패: " + e.getMessage());
}
//안전한 파싱
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
try {
    LocalDate date = LocalDate.parse("2025/10/02", formatter);
} catch (DateTimeParseException e) {
    System.out.println("형식이 맞지 않습니다.");
}
```
- (8) 예제
```java
//현재 시간을 원하는 형식으로
LocalDateTime now = LocalDateTime.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String timestamp = now.format(formatter);
//로그 파일명 생성
String logFileName = "log_" + LocalDate.now().format(
    DateTimeFormatter.ofPattern("yyyyMMdd")
) + ".txt";
//사용자 입력 파싱
String userInput = "2025-10-02 14:30";
DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
LocalDateTime parsedDateTime = LocalDateTime.parse(userInput, inputFormatter);
```
- 17. Period는 년, 월, 일을 독립적으로 저장
- 실제 경과한 총 일수가 아니라, 달력상의 차이를 표현
- P1M1D: "1개월 + 1일"이라는 개념만 저장, 실제로 며칠인지는 신경쓰지 않음
```java
LocalDate d1 = LocalDate.of(2025, 1, 31);
LocalDate d2 = LocalDate.of(2025, 3, 3);
Period period = Period.between(d1, d2);
System.out.println(period); //P1M3D
//1월 31일 → 2월 31일 (2월 28일인데) → 3월 3일
//= 1개월 + 3일
//실제로는 31일이지만 Period는 "1개월 3일"로 표현
```
- 실제 일수가 중요하다면 ChronoUnit.DAYS 사용
```java
LocalDate from = LocalDate.of(2025, 10, 2);
LocalDate to = LocalDate.of(2025, 11, 3);
long days = ChronoUnit.DAYS.between(from, to);
System.out.println("실제 경과 일수: " + days); //32일
```
- 시간 포함된 경우, Duration 사용
```java
LocalDateTime from = LocalDateTime.of(2025, 10, 2, 0, 0);
LocalDateTime to = LocalDateTime.of(2025, 11, 3, 0, 0);
Duration duration = Duration.between(from, to);
System.out.println("총 시간: " + duration.toHours()); //768시간
System.out.println("총 일수: " + duration.toDays()); //32일
```
- Period를 총 일수로 변환
```java
Period period = Period.between(from, to);
//시작일 기준으로 Period를 더해서 실제 일수 계산
long totalDays = from.until(from.plus(period), ChronoUnit.DAYS);
System.out.println("총 일수: " + totalDays);
```
# 8. 중첩 클래스, 내부 클래스1
- 0. 클래스 안에 클래스를 정의하면 중첩 클래스 (Nested Class)
- 정적 중첩 클래스는 정적 변수와 같은 위치에 선언한다.
- 정적 중첩 클래스는 정적 변수와 같이 static으로 선언한다.
- 내부 클래스는 인스턴스 변수와 같은 위치에 선언한다.
- 내부 클래스는 인스턴스 변수와 같이 non-static으로 선언한다.
- 지역 클래스는 지역 변수와 같은 위치에 선언한다. (코드 블럭 내)
- 익명 클래스는 이름이 없는 지역 클래스이다.
- 중첩 클래스 = 정적 중첩 + 내부 클래스를 말한다.
- 내부 클래스 = 내부 + 지역 + 익명 클래스를 말한다.
- 정적 중첩 클래스는 바깥 클래스 안에 있지만 관계 없는 다른 클래스이다.
- 내부 클래스는 안에 있으면서 바깥 클래스를 구성하는 요소가 된다.
- 바깥 클래스의 인스턴스에 소속이 된다 = 내부, 되지 않는다 = 정적 중첩
- 중첩 클래스 분류
- 1. 정적 중첩 클래스 (Static Nested Class)
- static으로 선언
- 외부 클래스의 정적 멤버처럼 동작
- 외부 클래스의 인스턴스 없이 사용 가능
- 2. 내부 클래스 (Inner Class)
- non-static으로 선언
- 외부 클래스의 인스턴스에 소속
- 외부 클래스의 멤버에 직접 접근 가능
- 3. 지역 클래스 (Local Class)
- 메서드나 블록 내부에 선언
- 해당 코드 블록 내에서만 유효
- 외부 지역 변수는 final 또는 effectively final일 때 접근 가능
- 4. 익명 클래스 (Anonymous Class)
- 이름 없이 선언
- 지역 클래스의 특수 형태
- 인터페이스나 클래스의 인스턴스를 즉시 구현할 때 사용
- 5. 중첩 클래스 예시
```java
class Outer {
    static class StaticNested {
        void display() {
            System.out.println("Static Nested Class");
        }
    }
    class Inner {
        void display() {
            System.out.println("Inner Class");
        }
    }
    void method() {
        class Local {
            void display() {
                System.out.println("Local Class");
            }
        }
        Local local = new Local();
        local.display();
        Runnable anonymous = new Runnable() {
            public void run() {
                System.out.println("Anonymous Class");
            }
        };
        anonymous.run();
    }
}
```
- 6. effectively final
```java
//지역 변수나 파라미터가 명시적으로 final로 선언되지 않았지만
//값이 한 번만 할당되고 이후 변경되지 않는 경우
//익명 클래스나 람다에서 외부 지역 변수를 사용할 때
//그 변수는 final 또는 effectively final이어야 한다.
//동시성 문제나 예측 불가능한 동작을 방지하기 위한 안전장치이다.
void example() {
    int x = 10; //x는 final이 아니지만, 이후 값이 바뀌지 않음
    Runnable r = new Runnable() {
        public void run() {
            System.out.println(x); //x는 effectively final이라 접근 가능
        }
    };
    r.run();
}
```
- 7. 내부 클래스(Inner Class)
```java
//외부 클래스의 멤버 변수나 메서드에는 자유롭게 접근할 수 있지만
//지역 변수는 final 또는 effectively final일 때만 접근할 수 있다.
//내부 클래스가 외부 클래스의 지역 변수에 접근하려면
//그 값을 내부적으로 복사해서 저장한다.
//만약 그 지역 변수가 이후에 변경된다면
//복사된 값과 실제 값이 달라질 수 있다.
class Outer {
    void method() {
        int x = 10; //effectively final
        class Inner {
            void print() {
                System.out.println(x); //가능
            }
        }
        x = 20; //이제 x는 effectively final이 아님 → 컴파일 에러 발생
    }
}
```
- 8. 내부 클래스에서 접근 가능 여부 정리
- (1) 외부 클래스의 인스턴스 변수: 가능
- (2) 외부 클래스의 메서드: 가능
- (3) 외부 클래스의 정적 변수: 가능 (단, 내부 클래스가 static이면 제한 있음)
- (4) 지역 변수 (final 또는 effectively final): 가능
- (5) 지역 변수 (값이 변경됨): 불가능
- 9. 정적 중첩 클래스 (static nested class)
```java
//static으로 선언된 중첩 클래스이며
//외부 클래스의 인스턴스와는 독립적으로 동작
//접근 가능한 멤버와 불가능한 멤버가 명확히 나뉨
//정적 중첩 클래스는 마치 외부 클래스의 정적 멤버처럼 동작하기 때문에
//인스턴스 멤버에는 직접 접근할 수 없다.
class Outer {
    static int staticVar = 100;
    int instanceVar = 200;
    static class StaticNested {
        void display() {
            System.out.println(staticVar); //가능
            System.out.println(instanceVar); //컴파일 에러
        }
    }
}
//instanceVar에 접근하고 싶다면, 외부 클래스의 인스턴스를 생성해서 참조
Outer outer = new Outer();
System.out.println(outer.instanceVar); //가능
```
- 10. 정적 중첩 클래스의 접근 가능 여부 정리
- (1) 외부 클래스의 정적 변수/메서드: 같은 정적 컨텍스트이므로 자유롭게 접근 가능
- (2) 외부 클래스의 인스턴스 변수/메서드: 외부 클래스의 인스턴스가 없기 때문에 직접 접근 불가
- (3) 외부 클래스의 다른 정적 중첩 클래스: 정적 멤버로 간주되므로 접근 가능
- (4) 외부 클래스의 생성자: 외부 클래스의 생성자를 통해 인스턴스를 만들 수는 있음
- 11. 중첩 클래스는 언제 사용하나?
- (1) 정적 중첩 클래스 (static nested class)
- 바깥 클래스의 인스턴스와는 독립적
- 바깥 클래스의 인스턴스 없이도 생성 가능
- 바깥 클래스와 밀접한 관계가 있지만
- 인스턴스 상태와는 무관한 클래스를 표현할 때 사용
```java
class Outer {
    static class Nested {
        void print() {
            System.out.println("정적 중첩 클래스");
        }
    }
}
public class Main {
    public static void main(String[] args) {
        Outer.Nested nested = new Outer.Nested();
        nested.print();
    }
}
//그밖의 예: Map.Entry
//Map 내부에서 key-value 쌍을 표현하는 클래스
```
- static nested class → 바깥 클래스와 관련 있지만 인스턴스와 무관할 때
- (2) 내부 클래스 (inner class)
- 바깥 클래스의 인스턴스와 연결되어 있음
- 바깥 클래스의 멤버(필드, 메서드 포함)에 직접 접근 가능
- 바깥 객체의 구현을 보조하거나
- 특정 객체 안에서만 의미 있는 클래스를 표현할 때 사용
```java
class Outer {
    private String message = "Hello";
    class Inner {
        void print() {
            System.out.println(message); //바깥 클래스의 private 멤버 접근 가능
        }
    }
}
public class Main {
    public static void main(String[] args) {
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
        inner.print();
    }
}
//그밖의 예: GUI 이벤트 처리기, 컬렉션의 iterator 구현
```
- inner class → 바깥 클래스 인스턴스에 종속적인 동작이 필요할 때
- (3) 지역 클래스 & 익명 클래스
- 지역 클래스: 메서드 안에 정의된 클래스
- → 해당 블록 안에서만 사용
- 익명 클래스: 이름 없이 바로 객체를 생성하는 클래스
- → 주로 일회성 이벤트 처리에 사용
```java
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("버튼 클릭됨!");
    }
});
```
- local/anonymous class → 일시적이고 짧은 범위에서만 쓸 때
- 12. 중첩 클래스 사용 이유
- (1) 관련된 클래스들을 하나의 바깥 클래스 안에 묶어 코드의 구조를 명확히 함
- (2) 외부에서 사용할 필요가 없는 클래스가 패키지 수준에서 드러나지 않도록 숨김
- (3) 바깥 클래스의 private 멤버에 접근할 수 있어 내부 구현을 효과적으로 다룰 수 있음
- (4) 불필요한 public 메서드를 만들지 않아도 되므로 외부 인터페이스를 깔끔하게 유지
- 13. 접근 예제1
```java
//staticNestedClass는 static nested class이므로
//outer 클래스의 static 멤버만 접근 가능
//static 메서드는 인스턴스 멤버에 접근 불가
//인스턴스 메서드는 자기 클래스의 인스턴스 멤버에는 접근 가능하지만
//outer 클래스의 인스턴스 멤버에는 접근 불가
public class StaticNestedOuter {
    private static int outerClassValue = 10;
    private int outerInstanceValue = 20;
    static class staticNestedClass {
        private static int innerClassValue = 100;
        private int innerInstanceValue = 200;
        public static void innerClassMethod() { //static 메서드
            System.out.println(outerClassValue); //가능: outer 클래스의 static 멤버
            //System.out.println(outerInstanceValue); //불가능: outer 클래스의 인스턴스 멤버
            System.out.println(innerClassValue); //가능: 자기 클래스의 static 멤버
            //System.out.println(innerInstanceValue); //불가능: static 메서드에서 인스턴스 멤버 접근 불가
            //innerInstanceMethod(); //불가능: static 메서드에서 인스턴스 메서드 호출 불가
        }
        public void innerInstanceMethod() { //인스턴스 메서드
            System.out.println(outerClassValue); //가능: outer 클래스의 static 멤버
            //System.out.println(outerInstanceValue); //불가능: outer 클래스의 인스턴스 멤버는 접근 불가
            System.out.println(innerClassValue); //가능: 자기 클래스의 static 멤버
            System.out.println(innerInstanceValue); //가능: 자기 클래스의 인스턴스 멤버
            innerClassMethod(); //가능: static 메서드 호출은 가능
        }
    }
    private static void outerClassMethod() { //static 메서드
        //innerClassMethod(); //불가능: staticNestedClass의 메서드는 직접 호출 불가
        //innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
        staticNestedClass.innerClassMethod(); //가능: static 메서드는 클래스명으로 호출 가능
        //staticNestedClass.innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
    }
    private void outerInstanceMethod() { //인스턴스 메서드
        //innerClassMethod(); //불가능: staticNestedClass의 메서드는 직접 호출 불가
        //innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
        staticNestedClass.innerClassMethod(); //가능: static 메서드는 클래스명으로 호출 가능
        //staticNestedClass.innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
    }
}
```
```java
public class StaticNestedMain {
    public static void main(String[] args) {
        StaticNestedOuter.staticNestedClass.innerClassMethod();
        StaticNestedOuter.staticNestedClass nested = new StaticNestedOuter.staticNestedClass(); //default 접근 제어
        nested.innerInstanceMethod();
    }
}
```
- 14. 접근 예제2
```java
//innerClass는 non-static inner class이므로
//outer 클래스의 모든 멤버에 접근 가능
//하지만 static 멤버를 가질 수 없음 (Java 21에서도 여전히 금지)
//static 메서드도 정의할 수 없음. → 컴파일 에러 발생
public class InnerOuter {
    private static int outerClassValue = 10;
    private int outerInstanceValue = 20;
    class innerClass {
        //private static int innerClassValue = 100; //불가능: inner class는 static 멤버를 가질 수 없음
        private int innerInstanceValue = 200;
        //public static void innerClassMethod() { //불가능: inner class는 static 메서드를 가질 수 없음
            //System.out.println(outerClassValue);
            ////System.out.println(outerInstanceValue);
            //System.out.println(innerClassValue);
            ////System.out.println(innerInstanceValue);
            ////innerInstanceMethod();
        //}
        public void innerInstanceMethod() { //인스턴스 메서드
            System.out.println(outerClassValue); //가능
            System.out.println(outerInstanceValue); //가능
            //System.out.println(innerClassValue); //static 멤버를 가질 수 없음 접근 불가
            System.out.println(innerInstanceValue); //가능
            //innerClassMethod(); //static 메서드가 정의 불가하므로 호출도 불가
        }
    }
    private static void outerClassMethod() {
        //innerClassMethod(); //innerClass는 인스턴스 클래스이므로 직접 호출 불가
        //innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
        //innerClass.innerClassMethod(); //innerClass는 인스턴스 클래스이므로 static 접근 불가
        //innerClass.innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
    }
    private void outerInstanceMethod() {
        //innerClassMethod(); //innerClass는 인스턴스 클래스이므로 직접 호출 불가
        //innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
        //innerClass.innerClassMethod(); //innerClass는 인스턴스 클래스이므로 static 접근 불가
        //innerClass.innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
    }
}
```
```java
public class InnerMain {
    public static void main(String[] args) {
        //InnerOuter.innerClass.innerClassMethod();
        //InnerOuter.innerClass nested = new InnerOuter.innerClass();
        //nested.innerInstanceMethod();
        //inner class는 외부 클래스의 인스턴스 없이 직접 생성할 수 없다.
        InnerOuter outer = new InnerOuter(); //외부 클래스 인스턴스 생성
        InnerOuter.innerClass nested = outer.new innerClass(); //inner 클래스 인스턴스 생성
        nested.innerInstanceMethod(); //메서드 호출
    }
}
```
- 15. A non-static inner class can declare static final fields
- but only if they are compile-time constants.
```java
class Outer {
    class Inner {
        static final int VALUE = 100;// 컴파일 타임 상수 → 허용
    }
}
class Outer {
    class Inner {
        static int value = 100; //static만 있는 경우 → 컴파일 에러
        static final int value2 = new Random().nextInt(); //컴파일 타임 상수가 아님 → 에러
    }
}
//컴파일 타임 상수란?
//리터럴 값 (int, String, boolean, char, double 등)
//public static final로 선언된 다른 컴파일 타임 상수
//수식이지만 모든 피연산자가 컴파일 타임 상수일 때
```
- 16. 기타: Java 메서드
- 변수에서는 static final이 자주 쓰이지만, static final 메서드 선언은 허용되지 않는다.
- static 메서드 = 클래스 메서드, final 메서드 = 오버라이딩 금지
- static final 메서드: static이 오버라이딩이 안 되므로 final이 불필요
- 오버라이딩 (Overriding): 인스턴스 메서드에만 해당됨
- 부모 클래스의 메서드를 자식이 재정의해서, 동적 바인딩(runtime에 결정)으로 호출
- static 메서드는 오버라이딩 불가
- static 메서드는 클래스에 속한 메서드 즉, 객체가 아니라 클래스 타입으로 결정됨
- 호출 시점에 컴파일 타임에 바인딩됨 (정적 바인딩)
- 자식이 같은 이름의 static 메서드를 정의하면, 오버라이딩이 아니라 숨김(hiding)이다.
- 숨김(hiding)은 다형성 적용이 불가능하다.
```java
class Parent {
    static void greet() {
        System.out.println("Hello from Parent");
    }
}
class Child extends Parent {
    static void greet() {
        System.out.println("Hello from Child");
    }
}
Parent p = new Child();
p.greet(); //"Hello from Parent" ← 정적 바인딩
//여기서 Child의 greet()는 Parent의 greet()을 숨긴 것
//Parent 타입으로 선언된 변수는 Parent의 static 메서드만 호출할 수 있다.
```
- 17. 기타2
- .getClass() 출력 결과: class 패키지명.바깥클래스명$중첩클래스명
- 접근 예제에서 중첩 클래스 인스턴스를 Main에서 생성했지만,
- Main에서 생성해 사용한다면 중첩 클래스 용도에 안 맞을 수 있다.
- 중첩 클래스를 private으로 선언하고..
- 중첩 클래스가 소속된 바깥 클래스 안에서 생성되고 사용되어야 한다.
- 18. 내부 클래스 로딩과 인스턴스 생성
- 바깥 클래스의 인스턴스를 여러 개 만들더라도, 내부 클래스의 바이트코드는 JVM에 한 번만 로딩됨
- 내부 클래스 인스턴스를 만들려면 반드시 바깥 클래스 인스턴스가 있어야 한다.
- JVM 내부적으로 내부 클래스는 Outer$Inner.class 같은 이름으로 컴파일된다.
- 내부 클래스 인스턴스는 생성될 때 Outer 인스턴스를 인자로 받는 생성자가 만들어진다.
- 내부 클래스 인스턴스가 바깥 클래스 인스턴스를 암묵적으로 참조하고 있기 때문에
- 바깥 클래스의 private 멤버까지 접근 가능하다.
- 컴파일러가 내부적으로 Outer.this라는 숨겨진 참조를 생성해서 연결해준다.
```java
class Outer {
    private int value = 42;
    class Inner {
        void print() {
            System.out.println(value); //Outer.this.value로 접근됨
        }
    }
}
```
- 19. 섀도잉
- 내부 클래스에서 바깥 클래스와 같은 이름의 필드나 메서드를 정의하면
- 내부 클래스의 멤버가 바깥 것을 가린다. (코드 블록으로 또 섀도잉 가능)
- this.는 내부 클래스의 멤버를 가리키고
- Outer.this.는 바깥 클래스의 인스턴스를 명시적으로 참조하므로
- 섀도잉이 있어도 접근 가능하다.
```java
public class ShadowingMain {
    public int value = 1;
    class Inner {
        public int value = 2;
        void go() {
            int value = 3;
            System.out.println("value = " + value);
            System.out.println("this.value = " + this.value);
            System.out.println("ShadowingMain.this.value = " + ShadowingMain.this.value);
        }
    }
    public static void main(String[] args) {
        ShadowingMain main = new ShadowingMain();
        main.new Inner().go();
    }
}
```
- 하지만 클래스 멤버에는 적용되지 않고 인스턴스에만 this 쓴다.
- 내부 클래스는 static이 아니어서, static 멤버를 가질 수 없다.
- this.staticValue나 Outer.this.staticValue는 존재하지 않는다.
- 바깥 static 멤버는 Outer.staticValue처럼 클래스명으로 접근한다.
- 20. static 클래스 안에서 this의 동작
- this는 자기 자신의 인스턴스를 가리킨다.
- static nested class의 static 멤버, 인스턴스 멤버에는 사용 가능하다.
- static nested class의 static 멤버는 보통 클래스명으로 접근한다. (staticInnerValue)
- Outer.this는 non-static inner class에서만 사용 가능하다.
- static nested는 Outer 인스턴스와 연결이 없기 때문에, Outer.this는 컴파일 에러 난다.
- Outer 클래스 멤버에는 Outer.로 접근 가능하다.
```java
public class Outer {
    private static int staticOuterValue = 10;
    private int instanceOuterValue = 20;
    static class Inner {
        private static int staticInnerValue = 100;
        private int instanceInnerValue = 200;
        void print() {
            System.out.println(this.instanceInnerValue); //자기 인스턴스 멤버
            System.out.println(this.staticInnerValue);        //자기 static 멤버
            System.out.println(Outer.staticOuterValue);        //바깥 클래스의 static 멤버
            //System.out.println(Outer.this.instanceOuterValue);   //바깥 클래스의 인스턴스 멤버 접근 불가
        }
    }
}
```
- (1) private static int staticOuterValue = 10;
- Outer 클래스가 JVM에 의해 처음 로딩될 때, static 필드가 메서드 영역(method area)에 올라가면서 초기화됨
- Outer 클래스가 참조되거나 인스턴스가 생성되지 않아도 초기화됨
- (2) private int instanceOuterValue = 20;
- Outer 클래스의 인스턴스를 생성할 때마다, 이 필드는 힙(heap)에 생성되고 초기화됨
- 즉, new Outer() 할 때마다 새로 만들어짐
- (3) private static int staticInnerValue = 100;
- Inner 클래스는 static 중첩 클래스이므로, JVM이 Outer.Inner를 처음 참조할 때 로딩되고 static 필드가 초기화됨
- Outer 클래스가 로딩돼도 Inner는 참조되지 않으면 로딩되지 않음
- (4) private int instanceInnerValue = 200;
- Inner 클래스의 인스턴스를 생성할 때마다 초기화됨
- 즉, new Outer.Inner() 또는 new Inner() (static이니까 가능) 할 때마다 새로 만들어짐
- (5) void print() {...}
- Inner.print(); // static이 아닌 메서드를 클래스 이름으로 호출 → 컴파일 에러
- (6) static void print() {...} 였다면?
- System.out.println(this.instanceInnerValue); //컴파일 오류: Cannot use 'this' in a static context
- System.out.println(instanceInnerValue); //컴파일 오류
- Inner inner = new Inner(); System.out.println(inner.instanceInnerValue); //가능
- 21. 정적 중첩 클래스의 접근 범위
- 바깥 클래스의 static 멤버 (static 변수, static 메서드): private도 바깥 클래스 같으므로 가능
- 바깥 클래스의 인스턴스 멤버 (public이든 private이든 상관없이): 접근 불가능
- 만약 접근하고 싶다면, Outer 클래스의 인스턴스를 생성해서 접근: private도 바깥 클래스 같으므로 가능
```java
public class Outer2 {
    private int secret = 42;
    static class Nested {
        Nested() {
            Outer2 outer = new Outer2();
            System.out.println(outer.secret); //된다.
            outer.hello(); //된다.
            outer.revealSecret(); //된다.
        }
    }
    private void revealSecret() {
        System.out.println(secret);
    }
    public void hello() {
        System.out.println("hello");
    }
}
```
---