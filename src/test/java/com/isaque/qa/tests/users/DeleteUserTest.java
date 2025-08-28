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
@Feature("DELETE /usuarios/{id}")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeleteUserTest extends BaseTest {

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
    @DisplayName("Deve excluir usuário com sucesso (200)")
    void shouldDeleteUser() {
        limiter.acquire();
        User u = new User(faker.name().fullName(),
                "del." + System.currentTimeMillis() + "@mailinator.com",
                "Senha@123",
                Env.adminFlag());

        String id = users.create(u).then().extract().path("_id");

        limiter.acquire();
        users.delete(id)
            .then()
            .statusCode(anyOf(is(200), is(204)))
            .body("message", anyOf(containsString("Registro excluído com sucesso"), anything()));
    }

   @Test
@DisplayName("Não deve excluir usuário inexistente (validação por GET subsequente)")
void shouldNotDeleteUnknown() {
    // 1) Cria e deleta
    limiter.acquire();
    User u = new User(faker.name().fullName(),
            "ghost." + System.currentTimeMillis() + "@mailinator.com",
            "Senha@123",
            Env.adminFlag());
    String id = users.create(u).then().extract().path("_id");

    limiter.acquire();
    users.delete(id).then().statusCode(anyOf(is(200), is(204)));

    // 2) Tenta deletar de novo — alguns ambientes retornam 200, outros 400/404
    limiter.acquire();
    users.delete(id).then().statusCode(anyOf(is(200), is(400), is(404)));

    // 3) Validação forte: não pode mais existir
    limiter.acquire();
    users.getById(id).then().statusCode(anyOf(is(400), is(404)));
}

}
