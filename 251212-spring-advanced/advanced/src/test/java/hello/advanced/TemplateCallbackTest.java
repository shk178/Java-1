package hello.advanced;

import hello.advanced.template.Callback;
import hello.advanced.template.Template;
import org.junit.jupiter.api.Test;

public class TemplateCallbackTest {
    @Test
    void one() {
        Template template = new Template();
        template.execute(new Callback() {
            @Override
            public void call() {
                System.out.println("test1");
            }
        });
    }
    @Test
    void two() {
        Template template = new Template();
        template.execute(() -> System.out.println("test2"));
    }
}
/*
test1
duration = 1
test2
duration = 0
 */