package be.dontletit.api;

import java.util.List;
import java.util.Optional;

public interface CardApiPort {
    Optional<CardResponse> getCardByName(String name);
    CardResponse getRandomCard();
    List<CardResponse> getRandomCards(int count);
}
