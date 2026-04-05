package com.team.book_exchange.support;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component("view")
public class ViewHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH);

    public String label(Enum<?> value) {
        if (value == null) {
            return "";
        }

        String[] words = value.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }

        return builder.toString();
    }

    public String price(BigDecimal value) {
        if (value == null) {
            return "Exchange only";
        }

        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0, RoundingMode.UNNECESSARY);
        }

        return "BDT " + normalized.toPlainString();
    }

    public String shortDate(LocalDate value) {
        return value == null ? "Repository history" : value.format(DATE_FORMATTER);
    }

    public String shortDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }

    public String initials(String value) {
        if (value == null || value.isBlank()) {
            return "BX";
        }

        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }
}
