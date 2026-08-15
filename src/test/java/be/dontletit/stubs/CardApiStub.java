package be.dontletit.stubs;

import be.dontletit.api.CardApiPort;
import be.dontletit.api.CardResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CardApiStub implements CardApiPort {

    public static final CardResponse FIXED_CARD = new CardResponse(
            "test-card", "Test Card", "{1}",
            List.of("INSTANT"),
            "Does nothing.",
            null, null, List.of(), List.of(), "", List.of("TST"), "TST", null);

    @Override
    public Optional<CardResponse> getCardByName(String name) {
        if ("Test Card".equalsIgnoreCase(name)) {
            return Optional.of(FIXED_CARD);
        }
        return Optional.empty();
    }

    @Override
    public CardResponse getRandomCard() {
        return FIXED_CARD;
    }

    @Override
    public List<CardResponse> getRandomCards(int count) {
        return Collections.nCopies(count, FIXED_CARD);
    }
}
