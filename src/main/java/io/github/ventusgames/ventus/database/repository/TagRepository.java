package io.github.ventusgames.ventus.database.repository;

import io.github.ventusgames.ventus.database.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Component
public interface TagRepository extends JpaRepository<Tag, String> {}
