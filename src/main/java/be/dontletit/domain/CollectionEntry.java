package be.dontletit.domain;

/** One card (by canonical name) and how many copies the collection holds. */
public record CollectionEntry(String cardName, int quantity) {}
