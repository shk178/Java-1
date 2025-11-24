package hello.itemservice.domain.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRepositoryTest {
    ItemRepository itemRepository = new ItemRepository();
    @AfterEach
    void afterEach() {
        itemRepository.clearStore();
    }
    @Test
    void save() {
        // given
        Item item = new Item("itemA", 10_000, 10);
        // when
        Item savedItem = itemRepository.save(item);
        // then
        Item findItem = itemRepository.findById(item.getId());
        assertThat(findItem).isEqualTo(savedItem);
    }
    @Test
    void findAll() {
        // given
        Item item1 = new Item("itemA", 10_000, 10);
        Item item2 = new Item("itemB", 20_000, 20);
        itemRepository.save(item1);
        itemRepository.save(item2);
        // when
        List<Item> result = itemRepository.findAll();
        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(item1, item2);
    }
    @Test
    void update() {
        // given
        Item item = new Item("itemA", 10_000, 10);
        Item savedItem = itemRepository.save(item);
        Long id = savedItem.getId();
        // when
        Item reference = new Item("itemB", 20_000, 20);
        itemRepository.update(id, reference);
        Item findItem = itemRepository.findById(id);
        // then
        assertThat(findItem.getName()).isEqualTo(reference.getName());
        assertThat(findItem.getPrice()).isEqualTo(reference.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(reference.getQuantity());
    }
}
