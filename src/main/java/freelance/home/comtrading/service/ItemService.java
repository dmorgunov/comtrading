package freelance.home.comtrading.service;

import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.domain.item.ParseStatus;
import freelance.home.comtrading.domain.item.Status;
import freelance.home.comtrading.domain.request.RequestTask;
import freelance.home.comtrading.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void updateFeedItems(List<Item> items) {
        itemRepository.saveAll(items);
    }

    public void updateFeedItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> getNotFilledItems(Integer limit) {
        return itemRepository.getNotFilledItems(Status.ADDED, PageRequest.of(0, limit));
    }

    public List<Item> getFilledItems(Integer page, Integer size) {
        List<Item> filledItems = itemRepository.getFilledItems(PageRequest.of(page, size));
        filledItems.removeIf(i->!i.getAutoSearchUrl().startsWith("http"));

        return filledItems;
    }

    public Item getItemByFeedId(Long feedId) {
        return itemRepository.getItemByFeedId(feedId);
    }

    public Item findByModel(String model) {
        return itemRepository.findByModel(model);
    }

    public List<Item> getItemsByFeedIds(Set<Long> feedIds) {
        return itemRepository.getItemsByFeedIds(feedIds);
    }

    public List<Item> getAllItems(Integer page, Integer size) {
        return itemRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    public Page<Item> findAllItems(Integer page, Integer size, String sort, String order, String status) {
        return itemRepository.customFindAll(ParseStatus.valueOf(status), PageRequest.of(
                page, size, (order.equals("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending()))
        );
    }

    public void updateUrl(Long feedId, String url) {
        itemRepository.updateUrl(feedId, url);
        itemRepository.updateParseStatus(feedId, ParseStatus.CONFIRMED);
    }

    public Page<Item> getItemsFilteredByStatus(String status) {
        return itemRepository.getItemsFilteredByStatus(
                Arrays.stream(status.split(",")).map(ParseStatus::valueOf).distinct().collect(Collectors.toList()),
                PageRequest.of(0, 1_000_000)
        );
    }
}
