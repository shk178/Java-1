package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }
    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 2_000) {
            errors.rejectValue("price", "range", new Object[]{1_000, 2_000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10) {
            errors.rejectValue("quantity", "range", new Object[]{1, 10}, null);
        }
    }
}
