package annotation;

import java.util.Arrays;

public class Main2 {
    public static void main(String[] args) {
        Class<ElementData> annoClass = ElementData.class;
        AnnoElement annotation = annoClass.getAnnotation(AnnoElement.class);
        System.out.println(annotation.value()); // data
        System.out.println(annotation.count()); // 10
        System.out.println(Arrays.toString(annotation.tags())); // [t1, t2]
        System.out.println(annotation.annoData()); // class network.MyLogger
    }
}
