package com.example.library;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class LibraryService {

    private final Map<Long, Book> books = new ConcurrentSkipListMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public LibraryService() {
        seed("Clean Code", "Robert C. Martin", "Technology",
                "A handbook of agile software craftsmanship.");
        seed("Effective Java", "Joshua Bloch", "Technology",
                "Best practices for writing good Java code.");
        seed("The Hobbit", "J.R.R. Tolkien", "Fantasy",
                "Bilbo Baggins joins a quest to reclaim a dwarf kingdom.");
        seed("1984", "George Orwell", "Dystopian",
                "A man struggles against a surveillance state.");
        seed("Dune", "Frank Herbert", "Science Fiction",
                "Politics and survival on the desert planet Arrakis.");
        seed("Pride and Prejudice", "Jane Austen", "Romance",
                "Elizabeth Bennet navigates manners and marriage.");
        seed("The Alchemist", "Paulo Coelho", "Fiction",
                "A shepherd travels in search of his destiny.");
        seed("Sapiens", "Yuval Noah Harari", "History",
                "A brief history of humankind.");
        seed("Atomic Habits", "James Clear", "Self-Help",
                "Small changes that build lasting habits.");
        seed("Murder on the Orient Express", "Agatha Christie", "Mystery",
                "Poirot investigates a murder aboard a snowbound train.");
    }

    private void seed(String name, String author, String genre, String description) {
        add(new BookRequest(name, author, genre, description));
    }

    public List<Book> findAll() {
        return List.copyOf(books.values());
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String q = query.toLowerCase();
        return books.values().stream()
                .filter(b -> contains(b.name(), q)
                        || contains(b.author(), q)
                        || contains(b.genre(), q))
                .toList();
    }

    public Book add(BookRequest request) {
        long id = nextId.getAndIncrement();
        Book book = new Book(id, request.name(), request.author(),
                request.genre(), request.description());
        books.put(id, book);
        return book;
    }

    public Optional<Book> update(Long id, BookRequest request) {
        if (!books.containsKey(id)) {
            return Optional.empty();
        }
        Book updated = new Book(id, request.name(), request.author(),
                request.genre(), request.description());
        books.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(Long id) {
        return books.remove(id) != null;
    }

    private boolean contains(String value, String lowerCaseQuery) {
        return value != null && value.toLowerCase().contains(lowerCaseQuery);
    }
}
