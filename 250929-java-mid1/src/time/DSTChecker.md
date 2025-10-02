# DST Checker Pseudo Code

## Main 함수
```
FUNCTION main():
    // 여러 타임존의 DST 사용 여부 확인
    CALL checkDST("America/New_York")
    CALL checkDST("Asia/Seoul")
    CALL checkDST("Europe/London")
    CALL checkDST("Australia/Sydney")
    CALL checkDST("UTC")
    
    PRINT "특정 날짜에 DST 적용 여부 확인"
    
    // 특정 날짜의 DST 적용 여부
    CALL checkDSTAtDate("America/New_York", date(2025, 7, 15))
    CALL checkDSTAtDate("America/New_York", date(2025, 1, 15))
    
    PRINT "DST 전환 시점 찾기"
    
    // 연도별 DST 전환 시점 찾기
    CALL findDSTTransitions("America/New_York", 2025)
END FUNCTION
```

## 방법 1: DST 사용 여부 확인
```
FUNCTION checkDST(zoneId):
    // 타임존 객체 생성
    zone = GET_TIMEZONE(zoneId)
    
    // 타임존 규칙 가져오기
    rules = zone.GET_RULES()
    
    // 현재 시간
    now = GET_CURRENT_TIME()
    
    // 현재 DST 적용 중인지 확인
    isDST = rules.IS_DAYLIGHT_SAVINGS(now)
    
    // 이 타임존이 DST 전환을 하는지 확인
    nextTransition = rules.GET_NEXT_TRANSITION(now)
    hasDST = (nextTransition IS NOT NULL) OR isDST
    
    // 결과 출력
    PRINT zoneId + ":"
    PRINT "  현재 DST 적용 중: " + isDST
    PRINT "  DST 제도 사용: " + hasDST
END FUNCTION
```

## 방법 2: 특정 날짜의 DST 적용 여부
```
FUNCTION checkDSTAtDate(zoneId, date):
    // 타임존 객체 생성
    zone = GET_TIMEZONE(zoneId)
    
    // 타임존 규칙 가져오기
    rules = zone.GET_RULES()
    
    // 날짜를 해당 타임존의 시작 시간으로 변환
    instant = CONVERT_TO_INSTANT(date, zone, START_OF_DAY)
    
    // 해당 시점에 DST 적용 여부 확인
    isDST = rules.IS_DAYLIGHT_SAVINGS(instant)
    
    // UTC 오프셋 가져오기
    offset = rules.GET_OFFSET(instant)
    
    // 결과 출력
    PRINT zoneId + " on " + date + ":"
    PRINT "  DST 적용: " + isDST
    PRINT "  UTC 오프셋: " + offset
END FUNCTION
```

## 방법 3: DST 전환 시점 찾기
```
FUNCTION findDSTTransitions(zoneId, year):
    // 타임존 객체 생성
    zone = GET_TIMEZONE(zoneId)
    
    // 타임존 규칙 가져오기
    rules = zone.GET_RULES()
    
    // 연도의 시작과 끝 시점 설정
    start = CREATE_INSTANT(year, 1, 1, 0, 0, zone)
    end = CREATE_INSTANT(year, 12, 31, 23, 59, zone)
    
    PRINT zoneId + " in " + year + ":"
    
    // 첫 번째 전환 시점 찾기
    transition = rules.GET_NEXT_TRANSITION(start)
    
    // 해당 연도 내의 모든 전환 시점 찾기
    WHILE (transition IS NOT NULL) AND (transition.time < end):
        transitionTime = transition.GET_INSTANT()
        zonedDateTime = CONVERT_TO_ZONED_TIME(transitionTime, zone)
        
        // 전환 정보 출력
        PRINT "  전환 시점: " + zonedDateTime
        PRINT "  전환 전 오프셋: " + transition.OFFSET_BEFORE
        PRINT "  전환 후 오프셋: " + transition.OFFSET_AFTER
        
        // 전환 타입 확인 (시작/종료)
        IF transition.IS_GAP():
            PRINT "  타입: 시작 (Spring Forward)"
        ELSE:
            PRINT "  타입: 종료 (Fall Back)"
        END IF
        
        // 다음 전환 시점 찾기
        transition = rules.GET_NEXT_TRANSITION(transitionTime)
    END WHILE
    
    // 전환이 없는 경우
    IF (transition IS NULL) OR (transition.time >= end):
        PRINT "  DST 전환 없음"
    END IF
END FUNCTION
```

## 핵심 개념 설명

### 데이터 구조
- **ZoneId**: 타임존 식별자 (예: "America/New_York")
- **ZoneRules**: 타임존의 규칙 (DST 전환 규칙 포함)
- **Instant**: UTC 기준 특정 시점
- **ZoneOffsetTransition**: DST 전환 정보

### 주요 알고리즘
1. **DST 확인**: 특정 시점의 규칙 조회
2. **전환 시점 찾기**: 시작 시점부터 반복적으로 다음 전환 조회
3. **전환 타입 판별**: Gap(시간 건너뜀) vs Overlap(시간 중복)

### 시간 복잡도
- `IS_DAYLIGHT_SAVINGS()`: O(1) - 규칙 테이블 조회
- `GET_NEXT_TRANSITION()`: O(log n) - 이진 탐색
- `findDSTTransitions()`: O(k) - k는 전환 횟수 (보통 0-2회/년)