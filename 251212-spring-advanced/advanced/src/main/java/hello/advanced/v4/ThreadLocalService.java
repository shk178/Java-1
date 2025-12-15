package hello.advanced.v4;

import org.springframework.stereotype.Service;

@Service
public class ThreadLocalService {
    private ThreadLocal<String> nameStore2 = new ThreadLocal<>();

    public String logic(String name2) {
        String thName = Thread.currentThread().getName();
        System.out.println("(저장 전) thName = " + thName + ", name2 = " + name2 + ", nameStore2.get() = " + nameStore2.get());
        nameStore2.set(name2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("(저장 후) thName = " + thName + ", name2 = " + name2 + ", nameStore2.get() = " + nameStore2.get());
        return nameStore2.get();
    }
}
