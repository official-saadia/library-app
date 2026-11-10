package com.example.library;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    private int statusOfGet(String uri) {
        return client().get().uri(uri)
                .exchange((request, response) -> response.getStatusCode().value());
    }

    private int statusOfPost(String json) {
        return client().post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .exchange((request, response) -> response.getStatusCode().value());
    }

    @Test
    void listReturnsSeededBooks() {
        String body = client().get().uri("/api/books").retrieve().body(String.class);

        assertThat(body).contains("Clean Code").contains("Dune").contains("The Hobbit");
    }

    @Test
    void getByIdReturnsBook() {
        String body = client().get().uri("/api/books/1").retrieve().body(String.class);

        assertThat(body).contains("Clean Code");
    }

    @Test
    void getMissingBookReturns404() {
        assertThat(statusOfGet("/api/books/9999")).isEqualTo(404);
    }

    @Test
    void searchReturnsMatchingBookOnly() {
        String body = client().get().uri("/api/books/search?q=hobbit")
                .retrieve().body(String.class);

        assertThat(body).contains("The Hobbit").doesNotContain("Dune");
    }

    @Test
    void addedBookCanBeFoundBySearch() {
        int status = statusOfPost("""
                {"name":"Neuromancer","author":"William Gibson",
                 "genre":"Cyberpunk","description":"A hacker takes one last job."}
                """);
        assertThat(status).isEqualTo(201);

        String body = client().get().uri("/api/books/search?q=cyberpunk")
                .retrieve().body(String.class);
        assertThat(body).contains("Neuromancer");
    }

    @Test
    void addingBookWithBlankNameReturns400() {
        int status = statusOfPost("""
                {"name":"","author":"Nobody","genre":"None","description":"Invalid"}
                """);

        assertThat(status).isEqualTo(400);
    }
}
