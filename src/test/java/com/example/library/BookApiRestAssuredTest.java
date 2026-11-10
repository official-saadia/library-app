package com.example.library;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * API tests written with REST Assured. They start the real app on a random
 * port and call it over HTTP, the same way a client (or curl) would.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiRestAssuredTest {

    @Value("${local.server.port}")
    private int port;

    private RequestSpecification api() {
        return given()
                .baseUri("http://localhost")
                .port(port)
                .basePath("/api/books");
    }

    private String bookJson(String name) {
        return """
                {"name":"%s","author":"Test Author","genre":"Testing","description":"Created by REST Assured"}
                """.formatted(name);
    }

    private int createBook(String name) {
        return api()
                .contentType(ContentType.JSON)
                .body(bookJson(name))
                .when().post()
                .then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void listBooksReturnsJsonArrayWithSeededBooks() {
        api()
                .when().get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(10))
                .body("name", hasItems("Clean Code", "Dune", "The Hobbit"));
    }

    @Test
    void getBookByIdReturnsAllFields() {
        api()
                .when().get("/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Clean Code"))
                .body("author", equalTo("Robert C. Martin"))
                .body("genre", equalTo("Technology"))
                .body("description", notNullValue());
    }

    @Test
    void getMissingBookReturns404() {
        api()
                .when().get("/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void searchByAuthorReturnsMatchingBook() {
        api()
                .queryParam("q", "tolkien")
                .when().get("/search")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("The Hobbit"));
    }

    @Test
    void searchWithNoMatchReturnsEmptyArray() {
        api()
                .queryParam("q", "no-such-book-anywhere")
                .when().get("/search")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void addBookReturns201AndGeneratedId() {
        api()
                .contentType(ContentType.JSON)
                .body(bookJson("Foundation"))
                .when().post()
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Foundation"))
                .body("genre", equalTo("Testing"));
    }

    @Test
    void addBookWithBlankNameReturns400() {
        api()
                .contentType(ContentType.JSON)
                .body(bookJson(""))
                .when().post()
                .then()
                .statusCode(400);
    }

    @Test
    void updateBookChangesItsFields() {
        int id = createBook("Original Title");

        api()
                .contentType(ContentType.JSON)
                .body(bookJson("Updated Title"))
                .when().put("/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("name", equalTo("Updated Title"));

        api()
                .when().get("/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Title"));
    }

    @Test
    void updateMissingBookReturns404() {
        api()
                .contentType(ContentType.JSON)
                .body(bookJson("Ghost Book"))
                .when().put("/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteBookRemovesIt() {
        int id = createBook("Book To Delete");

        api().when().delete("/" + id).then().statusCode(204);
        api().when().get("/" + id).then().statusCode(404);
        api().when().delete("/" + id).then().statusCode(404);
    }
}
