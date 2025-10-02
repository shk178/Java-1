package time;
import java.time.*;
import java.time.zone.*;

public class DSTChecker {

    public static void main(String[] args) {
        // 방법 1: ZoneRules를 이용한 확인
        checkDST("America/New_York");
        checkDST("Asia/Seoul");
        checkDST("Europe/London");
        checkDST("Australia/Sydney");
        checkDST("UTC");

        System.out.println("\n--- 특정 날짜에 DST 적용 여부 확인 ---");
        // 방법 2: 특정 날짜에 DST가 적용되는지 확인
        checkDSTAtDate("America/New_York", LocalDate.of(2025, 7, 15));  // 여름
        checkDSTAtDate("America/New_York", LocalDate.of(2025, 1, 15));  // 겨울

        System.out.println("\n--- DST 전환 시점 찾기 ---");
        // 방법 3: DST 전환 시점 찾기
        findDSTTransitions("America/New_York", 2025);
    }

    // 방법 1: 해당 타임존이 DST를 사용하는지 확인
    public static void checkDST(String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        ZoneRules rules = zone.getRules();

        // 현재 시점에서 DST 사용 여부
        Instant now = Instant.now();
        boolean isDST = rules.isDaylightSavings(now);

        // 이 타임존이 DST 전환을 하는지 확인 (과거/미래 포함)
        ZoneOffsetTransition nextTransition = rules.nextTransition(now);
        boolean hasDST = nextTransition != null || isDST;

        System.out.println(zoneId + ":");
        System.out.println("  현재 DST 적용 중: " + isDST);
        System.out.println("  DST 제도 사용: " + hasDST);
        System.out.println();
    }

    // 방법 2: 특정 날짜에 DST가 적용되는지 확인
    public static void checkDSTAtDate(String zoneId, LocalDate date) {
        ZoneId zone = ZoneId.of(zoneId);
        ZoneRules rules = zone.getRules();

        Instant instant = date.atStartOfDay(zone).toInstant();
        boolean isDST = rules.isDaylightSavings(instant);
        ZoneOffset offset = rules.getOffset(instant);

        System.out.println(zoneId + " on " + date + ":");
        System.out.println("  DST 적용: " + isDST);
        System.out.println("  UTC 오프셋: " + offset);
        System.out.println();
    }

    // 방법 3: 특정 연도의 DST 전환 시점 찾기
    public static void findDSTTransitions(String zoneId, int year) {
        ZoneId zone = ZoneId.of(zoneId);
        ZoneRules rules = zone.getRules();

        Instant start = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant();
        Instant end = LocalDate.of(year, 12, 31).atTime(23, 59).atZone(zone).toInstant();

        System.out.println(zoneId + " in " + year + ":");

        ZoneOffsetTransition transition = rules.nextTransition(start);
        while (transition != null && transition.getInstant().isBefore(end)) {
            Instant transitionTime = transition.getInstant();
            ZonedDateTime zdt = transitionTime.atZone(zone);

            System.out.println("  전환 시점: " + zdt);
            System.out.println("  전환 전 오프셋: " + transition.getOffsetBefore());
            System.out.println("  전환 후 오프셋: " + transition.getOffsetAfter());
            System.out.println("  타입: " + (transition.isGap() ? "시작 (Spring Forward)" : "종료 (Fall Back)"));
            System.out.println();

            transition = rules.nextTransition(transitionTime);
        }

        if (transition == null || !transition.getInstant().isBefore(end)) {
            System.out.println("  DST 전환 없음");
        }
    }
}