package de.skit.grocy.lists;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListRepository extends JpaRepository<ListEntity, UUID> {}
