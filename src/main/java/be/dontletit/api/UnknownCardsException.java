package be.dontletit.api;

import java.util.List;

/** Raised when a collection names cards that are not in the database. */
public class UnknownCardsException extends RuntimeException {

    private final List<String> unknownNames;

    public UnknownCardsException(List<String> unknownNames) {
        super("Unknown cards: " + String.join(", ", unknownNames));
        this.unknownNames = List.copyOf(unknownNames);
    }

    public List<String> unknownNames() {
        return unknownNames;
    }
}
