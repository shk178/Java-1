package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }
    /*
    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {
        // 검증 로직
        Map<String, String> errors = new HashMap<>();
        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "상품 이름 입력");
        }
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 2_000) {
            errors.put("price", "가격 1,000 ~ 2,000 입력");
        }
        if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10) {
            errors.put("quantity", "수량 1 ~ 10 입력");
        }
        // 검증 실패 -> 다시 입력 폼으로
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "validation/v1/addForm";
        }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }
     */
    @PostMapping("/add")
    public String addItem(
            @ModelAttribute Item item,
            RedirectAttributes redirectAttributes,
            BindingResult bindingResult
    ) {
        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름 입력"));
        }
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 2_000) {
            bindingResult.addError(new FieldError("item", "price", "가격 1,000 ~ 2,000 입력"));
        }
        if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10) {
            bindingResult.addError(new FieldError("item", "quantity", "수량 1 ~ 10 입력"));
        }
        // 검증 실패 -> 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            return "validation/v1/addForm";
        }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }

}

