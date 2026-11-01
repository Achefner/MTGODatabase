package be.dontletit.api;

/** Fills the card database from the external card catalog (Scryfall). */
public interface CardImportApiPort {

    /** Reads every printing of the catalog and stores it, replacing printings already known. */
    ImportResultResponse importCatalog();
}
