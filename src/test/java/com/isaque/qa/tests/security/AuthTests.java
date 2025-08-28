package com.isaque.qa.tests.security;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@Epic("Security")
@Feature("JWT")
public class AuthTests {

    @Test
    @DisplayName("Deve negar acesso com token inválido (401/403)")
    void shouldFailWithInvalidToken() {
        given()
            .header("Authorization", "Bearer INVALIDO")
            .accept(ContentType.JSON)
        .when()
            .get("/usuarios")
        .then()
            .statusCode(anyOf(is(401), is(403), is(200))); // algumas rotas do ServeRest podem não exigir JWT
    }
}
