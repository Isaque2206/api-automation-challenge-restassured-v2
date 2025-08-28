package com.isaque.qa.tests.users;

import com.isaque.qa.base.BaseTest;
import com.isaque.qa.client.AuthClient;
import com.isaque.qa.client.UsersClient;
import com.isaque.qa.util.Env;
import com.isaque.qa.util.RateLimiter;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Users API")
@Feature("GET /usuarios")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetUsersTest extends BaseTest {

    static UsersClient users;
    static RateLimiter limiter;

    @BeforeAll
    static void setupToken() {
        TOKEN = AuthClient.ensureToken();
        users = new UsersClient(TOKEN);
        limiter = new RateLimiter(Env.rateLimitInterval());
    }

    @Test
    @Order(1)
    @DisplayName("Deve listar usuários com schema válido")
    void shouldListUsers() {
        limiter.acquire();
        users.listAll()
            .then()
            .statusCode(200)
            .body("usuarios", notNullValue())
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/users-list-schema.json"));
    }
}
