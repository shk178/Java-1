package hello.itemservice.domain.item2;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemUpdateForm {
    @Range(min=2)
    private Long id;
    @NotBlank
    private String itemName;
    @NotNull
    @Range(min=100, max=200)
    private Integer price;
    @NotNull
    @Range(min=10, max=100)
    private Integer quantity;
}
