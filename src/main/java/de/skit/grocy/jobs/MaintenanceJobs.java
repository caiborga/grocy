package de.skit.grocy.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.items.ItemCleanupService;
import de.skit.grocy.security.session.AuthSessionRepository;

import java.time.OffsetDateTime;

@Component
public class MaintenanceJobs {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceJobs.class);

    private final ItemCleanupService cleanupService;
    private final AuthSessionRepository authSessionRepository;

    public MaintenanceJobs(ItemCleanupService cleanupService, AuthSessionRepository authSessionRepository) {
        this.cleanupService = cleanupService;
        this.authSessionRepository = authSessionRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupCheckedItems() {
        int deleted = cleanupService.cleanupCheckedItems();
        log.info("Checked-item cleanup finished, removed {}", deleted);
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupExpiredSessions() {
        int deleted = authSessionRepository.deleteExpired(OffsetDateTime.now());
        log.info("Expired session cleanup finished, removed {}", deleted);
    }
}
