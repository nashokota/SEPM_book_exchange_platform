package com.team.book_exchange.dto.view;

import java.time.LocalDate;

public record MilestoneView(
    String branchName,
    String branchLabel,
    String title,
    String category,
    String commitHash,
    LocalDate mergedDate
) {
}
