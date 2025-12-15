package hello.advanced.v3;

import org.springframework.stereotype.Service;

@Service
public class FieldService {
    private String nameStore;

    public String logic(String name) {
        String thName = Thread.currentThread().getName();
        System.out.println("(저장 전) thName = " + thName + ", name = " + name + ", nameStore = " + nameStore);
        nameStore = name;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("(저장 후) thName = " + thName + ", name = " + name + ", nameStore = " + nameStore);
        return nameStore;
    }
}
