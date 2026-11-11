package com.example.library;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LibraryServiceTest {

    private final LibraryService service = new LibraryService();

    @Test
    void seedsTenBooks() {
        assertThat(service.findAll()).hasSize(10);
    }

    @Test
    void findByIdReturnsSeededBook() {
        assertThat(service.findById(1L)).isPresent();
        assertThat(service.findById(1L).get().name()).isEqualTo("Clean Code");
    }

    @Test
    void findByIdReturnsEmptyForMissingBook() {
        assertThat(service.findById(999L)).isEmpty();
    }

    @Test
    void searchFindsBookByName() {
        assertThat(service.search("dune"))
                .extracting(Book::name)
                .containsExactly("Dune");
    }

    @Test
    void searchFindsBookByAuthor() {
        assertThat(service.search("tolkien"))
                .extracting(Book::name)
                .containsExactly("The Hobbit");
    }

    @Test
    void searchFindsBooksByGenre() {
        assertThat(service.search("technology")).hasSize(2);
    }

    @Test
    void searchWithNoMatchReturnsEmptyList() {
        assertThat(service.search("no-such-book")).isEmpty();
    }

    @Test
    void searchWithBlankQueryReturnsAllBooks() {
        assertThat(service.search("  ")).hasSize(10);
    }

    @Test
    void addAssignsNextIdAndStoresBook() {
        Book created = service.add(new BookRequest("Neuromancer", "William Gibson",
                "Science Fiction", "A hacker takes one last job."));

        assertThat(created.id()).isEqualTo(11L);
        assertThat(service.findAll()).hasSize(11);
        assertThat(service.findById(11L)).isPresent();
    }

    @Test
    void updateChangesExistingBook() {
        var updated = service.update(1L, new BookRequest("Clean Code 2nd Ed",
                "Robert C. Martin", "Technology", "Updated edition."));

        assertThat(updated).isPresent();
        assertThat(service.findById(1L).get().name()).isEqualTo("Clean Code 2nd Ed");
    }

    @Test
    void updateReturnsEmptyForMissingBook() {
        var updated = service.update(999L, new BookRequest("X", "Y", "Z", "W"));

        assertThat(updated).isEmpty();
    }

    @Test
    void deleteRemovesExistingBook() {
        assertThat(service.delete(1L)).isTrue();
        assertThat(service.findAll()).hasSize(9);
        assertThat(service.findById(1L)).isEmpty();
    }

    @Test
    void deleteReturnsFalseForMissingBook() {
        assertThat(service.delete(999L)).isFalse();
    }
}
