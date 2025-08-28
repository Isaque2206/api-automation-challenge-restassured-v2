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
@Feature("PUT /usuarios/{id}")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateUserTest extends BaseTest {

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
    @DisplayName("Deve atualizar usuário com sucesso (200)")
    void shouldUpdateUser() {
        limiter.acquire();
        User u = new User(faker.name().fullName(),
                "upd." + System.currentTimeMillis() + "@mailinator.com",
                "Senha@123",
                Env.adminFlag());

        String id = users.create(u).then().extract().path("_id");

        limiter.acquire();
        User updated = new User("Nome Atualizado " + faker.name().lastName(), u.email, u.password, "true");
        users.update(id, updated)
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("message", anyOf(containsString("Registro alterado com sucesso"), notNullValue()));

        limiter.acquire();
        users.getById(id)
            .then()
            .statusCode(200)
            .body("nome", containsString("Nome Atualizado"));
    }
@Test
@DisplayName("Atualização com id inexistente não deve reativar registro (validação por GET)")
void shouldNotUpdateWithInvalidId() {
    // 1) Cria e deleta um usuário para obter um ID inexistente controlado
    limiter.acquire();
    User u = new User(faker.name().fullName(),
            "upd-miss." + System.currentTimeMillis() + "@mailinator.com",
            "Senha@123",
            Env.adminFlag());
    String id = users.create(u).then().extract().path("_id");

    limiter.acquire();
    users.delete(id).then().statusCode(anyOf(is(200), is(204)));

    // 2) Tenta atualizar um ID já inexistente — alguns ambientes respondem 200/201
    limiter.acquire();
    User updated = new User("Nome X", u.email, u.password, "true");
    users.update(id, updated).then().statusCode(anyOf(is(200), is(201), is(400), is(404)));

    // 3) Validação forte: o recurso NÃO deve existir
    limiter.acquire();
    users.getById(id).then().statusCode(anyOf(is(400), is(404)));
}

}
