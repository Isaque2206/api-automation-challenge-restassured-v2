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
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Users API")
@Feature("POST /usuarios")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateUserTest extends BaseTest {

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
    @DisplayName("Deve criar usuário com sucesso (201)")
    void shouldCreateUser() {
        limiter.acquire();
        User u = new User(faker.name().fullName(),
                "qa." + System.currentTimeMillis() + "@mailinator.com",
                "Senha@123",
                Env.adminFlag());

        users.create(u)
            .then()
            .statusCode(anyOf(is(201), is(200)))
            .body("message", anyOf(containsString("Cadastro realizado com sucesso"), notNullValue()))
            .body("_id", notNullValue());
    }

    @Test
    @DisplayName("Não deve criar usuário sem campos obrigatórios (400)")
    void shouldNotCreateWithoutMandatoryFields() {
        limiter.acquire();
        User invalid = new User(null, null, null, null);
        users.create(invalid)
            .then()
            .statusCode(anyOf(is(400), is(422)));
    }

    @Test
    @DisplayName("Não deve criar usuário com e-mail duplicado (400/409)")
    void shouldNotCreateDuplicateEmail() {
        limiter.acquire();
        String email = "dup." + System.currentTimeMillis() + "@mailinator.com";
        User u1 = new User(faker.name().fullName(), email, "Senha@123", "true");
        users.create(u1).then().statusCode(anyOf(is(201), is(200)));

        limiter.acquire();
        User u2 = new User(faker.name().fullName(), email, "Senha@123", "true");
        users.create(u2)
            .then()
            .statusCode(anyOf(is(400), is(409)));
    }
}
