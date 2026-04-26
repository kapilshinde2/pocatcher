package com.boltech.pokemon.pocatcher.exception;

public class CatalogFetchInterruptedException extends RuntimeException {

    public CatalogFetchInterruptedException(String message, InterruptedException cause) {
        super(message, cause);
    }
}
