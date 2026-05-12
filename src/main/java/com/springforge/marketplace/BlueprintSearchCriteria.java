package com.springforge.marketplace;

import java.util.List;

public record BlueprintSearchCriteria(
    String query,
    BlueprintCategory category,
    List<String> tags,
    String author,
    Double minRating,
    String sortBy,
    String sortDir
) {}
