package de.skit.grocy.jobs;

import de.skit.grocy.items.ItemCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ItemCleanupJob {

    private final ItemCleanupService cleanupService;

    public ItemCleanupJob(ItemCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void run() {

        int deleted = cleanupService.cleanupCheckedItems();

        System.out.println(
                "Deleted checked items: " + deleted);
    }
}