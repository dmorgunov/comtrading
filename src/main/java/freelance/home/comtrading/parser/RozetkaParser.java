package freelance.home.comtrading.parser;

import freelance.home.comtrading.domain.aggregation.AggregationStatus;
import freelance.home.comtrading.domain.aggregation.Aggregator;
import freelance.home.comtrading.domain.aggregation.AggregatorComparingItem;
import freelance.home.comtrading.domain.item.Item;
import freelance.home.comtrading.repository.AggregatorComparingItemRepository;
import freelance.home.comtrading.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RozetkaParser {

    private final WebDriver webDriver;
    private final AggregatorComparingItemRepository aggregatorComparingItemRepository;
    private final ItemRepository itemRepository;

    @Scheduled(fixedDelay = 500000, initialDelay = 1)
    public void rozetkaParser() {
        while (true) {
            List<Item> firstNotProcessedByAggregator = itemRepository.getFirstNotProcessedByAggregator(Aggregator.ROZETKA, Pageable.ofSize(1));
            if (firstNotProcessedByAggregator.isEmpty()) {
                return;
            }
            Item item = firstNotProcessedByAggregator.get(0);

            String url = String.format("https://rozetka.com.ua/search/?text=%s", item.getName());
            webDriver.get(url);
            List<WebElement> elements = webDriver.findElements(By.cssSelector("div.goods-tile"));

            List<AggregatorComparingItem> result = new ArrayList<>();
            for (WebElement e : elements) {
                try {
                    result.add(getAggregatorComparingItem(item, url, e));
                } catch (Exception ex) {
                    // extinguish the error
                    log.error("Error parsing url=\"{}\"", url, ex);
                }
            }
            if (result.isEmpty()) {
                // rozetka redirects on good's page if there are ony 1 good founded
                result.add(getSingleAggregatorComparingItem(item, url));
            }

            if (result.isEmpty()) {
                result.add(getFailedAggregatorComparingItem(item, url));
            }

            aggregatorComparingItemRepository.saveAll(result);
            log.info("Parsed {} items for query {}", result.size(), item.getName());
        }
    }

    private AggregatorComparingItem getSingleAggregatorComparingItem(Item item, String url) {

        String price = "0";
        try {
            // single price
            price = webDriver.findElement(By.cssSelector("p.product-prices__big")).getText();
            price = price.replaceAll(" ", "").replaceAll("₴", "");
        } catch (NoSuchElementException e) {
        }

        Boolean available;
        try {
            webDriver.findElement(By.cssSelector("p.status-label--green"));
            available = true;
        } catch (NoSuchElementException ex) {
            available = false;
        }

        return new AggregatorComparingItem()
                .setItem(item)
                .setAggregator(Aggregator.ROZETKA)
                .setAggregationStatus(AggregationStatus.SUCCESS)
                .setPrice(new BigDecimal(price))
                .setLink(url)
                .setAvailable(available);
    }


    private AggregatorComparingItem getAggregatorComparingItem(Item item, String url, WebElement e) {
        String price;
        try {
            price = e.findElement(By.cssSelector("span.goods-tile__price-value")).getText();
            price = price.replaceAll(" ", "");
        } catch (NoSuchElementException ex) {
            price = "0";
        }

        Boolean available;
        try {
            e.findElement(By.cssSelector("div.goods-tile__availability--available"));
            available = true;
        } catch (NoSuchElementException ex) {
            available = false;
        }

        return new AggregatorComparingItem()
                .setItem(item)
                .setAggregator(Aggregator.ROZETKA)
                .setAggregationStatus(AggregationStatus.SUCCESS)
                .setPrice(new BigDecimal(price))
                .setLink(url)
                .setAvailable(available);
    }

    private AggregatorComparingItem getFailedAggregatorComparingItem(Item item, String url) {
        return new AggregatorComparingItem()
                .setItem(item)
                .setAggregator(Aggregator.ROZETKA)
                .setAggregationStatus(AggregationStatus.FAIL)
                .setPrice(null)
                .setLink(url)
                .setAvailable(null);
    }
}
