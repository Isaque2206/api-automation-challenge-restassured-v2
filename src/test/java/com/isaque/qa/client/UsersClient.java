package com.isaque.qa.client;

import com.isaque.qa.model.User;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UsersClient {

    private final String token;

    public UsersClient(String token) {
        this.token = token;
    }

    public Response listAll() {
        return given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/usuarios");
    }

    public Response create(User user) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(user)
            .when()
                .post("/usuarios");
    }

    public Response getById(String id) {
        return given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .get("/usuarios/{id}", id);
    }

    public Response update(String id, User user) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(user)
            .when()
                .put("/usuarios/{id}", id);
    }

    public Response delete(String id) {
        return given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
            .when()
                .delete("/usuarios/{id}", id);
    }
}
