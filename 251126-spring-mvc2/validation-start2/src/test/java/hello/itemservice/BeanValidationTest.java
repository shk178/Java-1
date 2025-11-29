package hello.itemservice;

import hello.itemservice.domain.item.Item;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class BeanValidationTest {
    @Test
    void beanValidation() {
        // 검증기 생성
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Item item = new Item();
        item.setItemName("  ");
        item.setPrice(1001);
        item.setQuantity(1);
        // 검증 실행
        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        for (ConstraintViolation<Item> violation : violations) {
            System.out.println(violation);
            System.out.println(violation.getMessage());
        }
        /*
        ConstraintViolationImpl{interpolatedMessage='공백일 수 없습니다', propertyPath=itemName, rootBeanClass=class hello.itemservice.domain.item.Item, messageTemplate='{javax.validation.constraints.NotBlank.message}'}
공백일 수 없습니다
        ConstraintViolationImpl{interpolatedMessage='10에서 100 사이여야 합니다', propertyPath=quantity, rootBeanClass=class hello.itemservice.domain.item.Item, messageTemplate='{org.hibernate.validator.constraints.Range.message}'}
        10에서 100 사이여야 합니다
        ConstraintViolationImpl{interpolatedMessage='100에서 200 사이여야 합니다', propertyPath=price, rootBeanClass=class hello.itemservice.domain.item.Item, messageTemplate='{org.hibernate.validator.constraints.Range.message}'}
        100에서 200 사이여야 합니다
         */
    }
}
