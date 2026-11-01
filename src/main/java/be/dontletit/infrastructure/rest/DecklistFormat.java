package be.dontletit.infrastructure.rest;

import be.dontletit.api.CollectionEntryRequest;
import be.dontletit.api.CollectionEntryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The plain-text decklist format most tools exchange (a wire format of the REST adapter): one card per line, optionally
 * prefixed with a quantity ("4 Lightning Bolt", "4x Lightning Bolt", "Lightning Bolt").
 * Blank lines, "#" / "//" comments, section headers and Arena's "(SET) 123" suffix are ignored.
 */
public final class DecklistFormat {

    private static final Pattern QUANTITY_PREFIX = Pattern.compile("^(\\d+)[xX]?\\s+(.+)$");
    private static final Pattern ARENA_SUFFIX = Pattern.compile("\\s+\\([A-Za-z0-9]{2,6}\\)\\s+[A-Za-z0-9-]+$");
    private static final Set<String> SECTION_HEADERS = Set.of("deck", "main", "maindeck", "mainboard", "sideboard", "commander", "companion");

    private DecklistFormat() {}

    public static List<CollectionEntryRequest> parse(String text) {
        List<CollectionEntryRequest> entries = new ArrayList<>();
        if (text == null) return entries;
        for (String rawLine : text.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;
            if (SECTION_HEADERS.contains(line.toLowerCase())) continue;

            int quantity = 1;
            String name = line;
            Matcher matcher = QUANTITY_PREFIX.matcher(line);
            if (matcher.matches()) {
                quantity = Integer.parseInt(matcher.group(1));
                name = matcher.group(2).trim();
            }
            name = ARENA_SUFFIX.matcher(name).replaceFirst("");
            entries.add(new CollectionEntryRequest(name, quantity));
        }
        return entries;
    }

    public static String format(List<CollectionEntryResponse> entries) {
        StringBuilder out = new StringBuilder();
        for (CollectionEntryResponse entry : entries) {
            out.append(entry.quantity()).append(' ').append(entry.cardName()).append('\n');
        }
        return out.toString();
    }
}
