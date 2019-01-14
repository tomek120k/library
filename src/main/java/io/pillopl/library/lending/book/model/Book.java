package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.aggregates.Version;

public interface Book {

    default BookId bookId() {
        return getBookInformation().getBookId();
    }

    BookInformation getBookInformation();

    Version getVersion();

}
