package hello.advanced.template;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTemplate {
    public void execute() {
        log.info("AbstractTemplate.execute run");
        long sTime = System.currentTimeMillis();
        call();
        long eTime = System.currentTimeMillis();
        log.info("duration = {}ms", eTime - sTime);
    }
    protected abstract void call();
    // 상속 클래스에서 오버라이딩 or 익명 클래스에서 오버라이딩
}
