package hello.itemservice.web.item.basic;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/basic/items")
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class BasicItemController {
    private final ItemRepository itemRepository;
    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "basic/items";
    }
    // 테스트용 데이터 추가
    @PostConstruct
    public void init() {
        itemRepository.save(new Item("testA", 10_000, 10));
        itemRepository.save(new Item("testB", 20_000, 20));
    }
    @GetMapping("/{id}")
    public String item(@PathVariable("id") Long id, Model model) {
        Item findItem = itemRepository.findById(id);
        model.addAttribute("item", findItem);
        return "basic/item";
    }
    @GetMapping("/add")
    public String addForm() {
        return "basic/addForm";
    }
    @PostMapping("/add")
    public String addItem(Item item, RedirectAttributes ras) {
        return addItemV6(item, ras);
    }
    @PostMapping("/add/v1")
    public String addItemV1(
            @RequestParam("name") String name,
            @RequestParam("price") int price,
            @RequestParam("quantity") Integer quantity,
            Model model
    ) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setQuantity(quantity);
        itemRepository.save(item);
        model.addAttribute("item", item);
        return "basic/item";
    }
    @PostMapping("/add/v2")
    public String addItemV2(
            @ModelAttribute("item") Item item,
            Model model
    ) {
        itemRepository.save(item);
        // model에 item 자동 추가 ("item"이 이름)
        return "basic/item";
    }
    @PostMapping("/add/v3")
    public String addItemV3(
            @ModelAttribute Item item,
            Model model
    ) {
        itemRepository.save(item);
        // model에 item 자동 추가 ("item"이 이름 자동)
        return "basic/item";
    }
    @PostMapping("/add/v4")
    public String addItemV4(
            Item item
    ) {
        itemRepository.save(item);
        return "basic/item";
    }
    @PostMapping("/add/v5")
    public String addItemV5(
            Item item
    ) {
        itemRepository.save(item);
        return "redirect:/basic/items/" + item.getId();
    }
    @PostMapping("/add/v6")
    public String addItemV6(
            Item item,
            RedirectAttributes ras
    ) {
        Item savedItem = itemRepository.save(item);
        ras.addAttribute("id", savedItem.getId());
        ras.addAttribute("status", true);
        // {id}는 pathVariable 바인딩 되고 status는 쿼리 파라미터가 된다.
        return "redirect:/basic/items/{id}";
    }
    @GetMapping("/{id}/edit")
    public String editFrom(@PathVariable("id") Long id, Model model) {
        Item findItem = itemRepository.findById(id);
        model.addAttribute("item", findItem);
        return "basic/editForm";
    }
    @PostMapping("/{id}/edit")
    public String editItem(@PathVariable("id") Long id, @ModelAttribute Item item) {
        itemRepository.update(id, item);
        return "redirect:/basic/items/{id}";
    }
}
