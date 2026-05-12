package com.springforge.marketplace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlueprintServiceTest {

    @Mock
    private BlueprintRepository blueprintRepository;

    @Mock
    private BlueprintCommentRepository commentRepository;

    @InjectMocks
    private BlueprintService service;

    private Blueprint sampleBlueprint;

    @BeforeEach
    void setUp() {
        sampleBlueprint = new Blueprint();
        sampleBlueprint.setId("bp-1");
        sampleBlueprint.setName("Microservice Starter");
        sampleBlueprint.setDescription("A starter blueprint for microservices");
        sampleBlueprint.setAuthor("admin");
        sampleBlueprint.setVersion("1.0.0");
        sampleBlueprint.setCategory(BlueprintCategory.MICROSERVICE);
        sampleBlueprint.setDownloads(0);
        sampleBlueprint.setRating(0);
        sampleBlueprint.setRatingCount(0);
    }

    @Test
    void shouldCreateBlueprintWithDefaults() {
        when(blueprintRepository.save(any())).thenReturn(sampleBlueprint);

        Blueprint result = service.create(sampleBlueprint);

        assertThat(result).isNotNull();
        verify(blueprintRepository).save(sampleBlueprint);
        assertThat(sampleBlueprint.isPublished()).isFalse();
        assertThat(sampleBlueprint.isVerified()).isFalse();
    }

    @Test
    void shouldUpdateExistingBlueprint() {
        when(blueprintRepository.findById("bp-1")).thenReturn(Optional.of(sampleBlueprint));
        when(blueprintRepository.save(any())).thenReturn(sampleBlueprint);

        Blueprint updated = new Blueprint();
        updated.setName("Updated Name");
        updated.setDescription("Updated description");
        updated.setVersion("2.0.0");
        updated.setCategory(BlueprintCategory.REST_API);

        Blueprint result = service.update("bp-1", updated);

        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldThrowOnUpdateNonExistent() {
        when(blueprintRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("unknown", new Blueprint()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void shouldPublishBlueprint() {
        when(blueprintRepository.findById("bp-1")).thenReturn(Optional.of(sampleBlueprint));
        when(blueprintRepository.save(any())).thenReturn(sampleBlueprint);

        service.publish("bp-1");

        assertThat(sampleBlueprint.isPublished()).isTrue();
    }

    @Test
    void shouldCalculateAverageRating() {
        sampleBlueprint.setRating(4.0);
        sampleBlueprint.setRatingCount(2);
        when(blueprintRepository.findById("bp-1")).thenReturn(Optional.of(sampleBlueprint));
        when(blueprintRepository.save(any())).thenReturn(sampleBlueprint);

        service.rate("bp-1", new RatingRequest(5, "Great!"));

        assertThat(sampleBlueprint.getRatingCount()).isEqualTo(3);
        assertThat(sampleBlueprint.getRating()).isCloseTo(4.33, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void shouldIncrementDownloadCount() {
        sampleBlueprint.setDownloads(10);
        when(blueprintRepository.findById("bp-1")).thenReturn(Optional.of(sampleBlueprint));
        when(blueprintRepository.save(any())).thenReturn(sampleBlueprint);

        service.download("bp-1");

        assertThat(sampleBlueprint.getDownloads()).isEqualTo(11);
    }

    @Test
    void shouldSearchByQuery() {
        Page<Blueprint> page = new PageImpl<>(List.of(sampleBlueprint));
        when(blueprintRepository.search(eq("micro"), any())).thenReturn(page);

        BlueprintSearchCriteria criteria = new BlueprintSearchCriteria("micro", null, null, null, null, null, null);
        Page<Blueprint> result = service.search(criteria, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnPopularBlueprints() {
        when(blueprintRepository.findTop10ByPublishedTrueOrderByDownloadsDesc()).thenReturn(List.of(sampleBlueprint));

        List<Blueprint> popular = service.getPopular();

        assertThat(popular).hasSize(1);
    }
}
