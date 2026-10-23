package be.dontletit.api;

import java.time.LocalDate;

/** A set in which at least one card was first printed. */
public record SetResponse(
        String code,
        String name,
        String type,
        LocalDate releasedAt,
        int cardCount
) {}
