package com.springforge.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlueprintCommentRepository extends JpaRepository<BlueprintComment, String> {

    Page<BlueprintComment> findByBlueprintIdOrderByCreatedAtDesc(String blueprintId, Pageable pageable);
}
