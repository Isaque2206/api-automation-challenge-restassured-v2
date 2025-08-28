package com.isaque.qa.client;

import com.github.javafaker.Faker;
import com.isaque.qa.model.LoginRequest;
import com.isaque.qa.model.LoginResponse;
import com.isaque.qa.model.User;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class AuthClient {

    public static String ensureToken() {
        Faker faker = new Faker();
        String email = "qa." + System.currentTimeMillis() + "@mailinator.com";
        String password = "Senha@123";

        User u = new User(faker.name().fullName(), email, password, "true");
        given()
            .contentType(ContentType.JSON)
            .body(u)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(org.hamcrest.Matchers.isOneOf(201, 200));

        LoginRequest req = new LoginRequest(email, password);
        LoginResponse resp =
        given()
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .extract().as(LoginResponse.class);

        return resp.getToken();
    }
}
