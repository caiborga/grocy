package de.skit.grocy.items;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ItemCleanupService {

    private final ItemRepository itemRepository;

    public ItemCleanupService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public int cleanupCheckedItems() {
        OffsetDateTime cutoff =
            OffsetDateTime.now().minusDays(14);

        return itemRepository
            .deleteCheckedItemsOlderThan(cutoff);
    }
}