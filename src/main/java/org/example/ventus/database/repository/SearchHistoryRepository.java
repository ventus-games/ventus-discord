package org.example.ventus.database.repository;

import org.example.ventus.database.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Component
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {}
