package com.isaque.qa.tests.users;

import com.github.javafaker.Faker;
import com.isaque.qa.base.BaseTest;
import com.isaque.qa.client.AuthClient;
import com.isaque.qa.client.UsersClient;
import com.isaque.qa.model.User;
import com.isaque.qa.util.Env;
import com.isaque.qa.util.RateLimiter;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Users API")
@Feature("GET /usuarios/{id}")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetUserByIdTest extends BaseTest {

    static UsersClient users;
    static RateLimiter limiter;
    static Faker faker;

    @BeforeAll
    static void setup() {
        TOKEN = AuthClient.ensureToken();
        users = new UsersClient(TOKEN);
        limiter = new RateLimiter(Env.rateLimitInterval());
        faker = new Faker();
    }

    @Test
    @DisplayName("Deve obter usuário por ID com sucesso (200)")
    void shouldGetUserById() {
        limiter.acquire();
        User u = new User(faker.name().fullName(),
                "byid." + System.currentTimeMillis() + "@mailinator.com",
                "Senha@123",
                Env.adminFlag());

        String id = users.create(u).then().extract().path("_id");

        limiter.acquire();
        users.getById(id)
            .then()
            .statusCode(200)
            .body("_id", equalTo(id))
            .body("email", equalTo(u.email))
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar ID inexistente")
    void shouldReturn404ForUnknownId() {
        limiter.acquire();
        users.getById("123invalid123")
            .then()
            .statusCode(anyOf(is(400), is(404)));
    }
}
