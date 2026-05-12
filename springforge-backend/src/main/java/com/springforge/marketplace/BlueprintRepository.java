package com.springforge.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, String> {

    Page<Blueprint> findByPublishedTrue(Pageable pageable);

    Page<Blueprint> findByCategory(BlueprintCategory category, Pageable pageable);

    Page<Blueprint> findByAuthor(String author, Pageable pageable);

    @Query("SELECT b FROM Blueprint b WHERE b.published = true AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Blueprint> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Blueprint b WHERE b.published = true AND b.rating >= :minRating")
    Page<Blueprint> findByMinRating(@Param("minRating") double minRating, Pageable pageable);

    List<Blueprint> findTop10ByPublishedTrueOrderByDownloadsDesc();

    List<Blueprint> findTop10ByPublishedTrueOrderByRatingDesc();

    List<Blueprint> findByPublishedFalseAndVerifiedFalse();
}
