package com.isaque.qa.base;

import io.qameta.allure.junit5.AllureJunit5;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllureJunit5.class) // habilita o listener do Allure para JUnit 5
public abstract class BaseTest {

    protected static String BASE_URL;
    protected static String TOKEN;

    @BeforeAll
    public static void globalSetup() {
        BASE_URL = System.getProperty("baseUrl",
                System.getenv().getOrDefault("BASE_URL", "https://serverest.dev"));

        RestAssured.baseURI = BASE_URL;

        // Anexa request/response no Allure automaticamente
        RestAssured.filters(new AllureRestAssured());

        // Loga request/response somente quando a asserção falhar (útil para depurar)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Parser padrão como JSON
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void tearDown() {
        // noop
    }
}
