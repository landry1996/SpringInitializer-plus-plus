package com.springforge.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BlueprintService {

    private final BlueprintRepository blueprintRepository;
    private final BlueprintCommentRepository commentRepository;

    public BlueprintService(BlueprintRepository blueprintRepository, BlueprintCommentRepository commentRepository) {
        this.blueprintRepository = blueprintRepository;
        this.commentRepository = commentRepository;
    }

    public Blueprint create(Blueprint blueprint) {
        blueprint.setDownloads(0);
        blueprint.setRating(0);
        blueprint.setRatingCount(0);
        blueprint.setPublished(false);
        blueprint.setVerified(false);
        return blueprintRepository.save(blueprint);
    }

    public Blueprint update(String id, Blueprint updated) {
        Blueprint existing = blueprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blueprint not found: " + id));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setVersion(updated.getVersion());
        existing.setCategory(updated.getCategory());
        existing.setTags(updated.getTags());
        existing.setConfigurationJson(updated.getConfigurationJson());
        return blueprintRepository.save(existing);
    }

    public void delete(String id) {
        blueprintRepository.deleteById(id);
    }

    public Blueprint publish(String id) {
        Blueprint bp = blueprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blueprint not found: " + id));
        bp.setPublished(true);
        return blueprintRepository.save(bp);
    }

    public Page<Blueprint> search(BlueprintSearchCriteria criteria, Pageable pageable) {
        if (criteria.query() != null && !criteria.query().isBlank()) {
            return blueprintRepository.search(criteria.query(), pageable);
        }
        if (criteria.category() != null) {
            return blueprintRepository.findByCategory(criteria.category(), pageable);
        }
        if (criteria.author() != null && !criteria.author().isBlank()) {
            return blueprintRepository.findByAuthor(criteria.author(), pageable);
        }
        if (criteria.minRating() != null) {
            return blueprintRepository.findByMinRating(criteria.minRating(), pageable);
        }
        return blueprintRepository.findByPublishedTrue(pageable);
    }

    public Blueprint rate(String id, RatingRequest request) {
        Blueprint bp = blueprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blueprint not found: " + id));

        double totalRating = bp.getRating() * bp.getRatingCount();
        bp.setRatingCount(bp.getRatingCount() + 1);
        bp.setRating((totalRating + request.rating()) / bp.getRatingCount());

        if (request.comment() != null && !request.comment().isBlank()) {
            BlueprintComment comment = new BlueprintComment();
            comment.setBlueprintId(id);
            comment.setAuthor("anonymous");
            comment.setContent(request.comment());
            commentRepository.save(comment);
        }

        return blueprintRepository.save(bp);
    }

    public Blueprint download(String id) {
        Blueprint bp = blueprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blueprint not found: " + id));
        bp.setDownloads(bp.getDownloads() + 1);
        return blueprintRepository.save(bp);
    }

    public List<Blueprint> getPopular() {
        return blueprintRepository.findTop10ByPublishedTrueOrderByDownloadsDesc();
    }

    public List<Blueprint> getTopRated() {
        return blueprintRepository.findTop10ByPublishedTrueOrderByRatingDesc();
    }

    public Blueprint getById(String id) {
        return blueprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blueprint not found: " + id));
    }

    public List<Blueprint> getPendingApproval() {
        return blueprintRepository.findByPublishedFalseAndVerifiedFalse();
    }

    public Blueprint approve(String id) {
        Blueprint bp = blueprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blueprint not found: " + id));
        bp.setVerified(true);
        bp.setPublished(true);
        return blueprintRepository.save(bp);
    }
}
