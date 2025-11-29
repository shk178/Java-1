package hello.itemservice.domain.item;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Item {
    @Range(min=2, groups = {UpdateCheck.class})
    private Long id;
    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;
    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    @Range(min=100, max=200, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;
    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    @Range(min=10, max=100, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
