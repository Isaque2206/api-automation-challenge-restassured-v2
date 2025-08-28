package com.isaque.qa.tests.limits;

import com.isaque.qa.base.BaseTest;
import com.isaque.qa.client.AuthClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Limits")
@Feature("Rate limiting")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class RateLimitTest extends BaseTest {

    // Parâmetros via -D... (ou variáveis de ambiente equivalentes)
    static boolean ENABLED;
    static String PATH;
    static int BURST;
    static int EXPECTED_STATUS;
    static int ATTEMPTS;
    static long SLEEP_MS;
    static int PROBE_CALLS; // chamadas rápidas para detectar suporte a rate-limit

    @BeforeAll
    static void setup() {
        ENABLED = boolProp("rate.limit.enabled", "RATE_LIMIT_ENABLED", false);
        PATH = strProp("rate.limit.path", "RATE_LIMIT_PATH", "/usuarios");
        BURST = intProp("rate.limit.burst", "RATE_LIMIT_BURST", 25);
        EXPECTED_STATUS = intProp("rate.limit.status", "RATE_LIMIT_STATUS", 429);
        ATTEMPTS = intProp("rate.limit.attempts", "RATE_LIMIT_ATTEMPTS", BURST + 20);
        SLEEP_MS = longProp("rate.limit.sleep", "RATE_LIMIT_SLEEP", 0L);
        PROBE_CALLS = intProp("rate.limit.probe", "RATE_LIMIT_PROBE", 5);

        Assumptions.assumeTrue(ENABLED,
                "Teste de rate-limit desabilitado. Habilite com -Drate.limit.enabled=true");

        // Token se necessário pela sua API
        TOKEN = AuthClient.ensureToken();

        System.out.printf(Locale.ROOT,
                "[RateLimitTest] enabled=%s, path=%s, burst=%d, expected=%d, attempts=%d, sleep=%dms, probe=%d%n",
                ENABLED, PATH, BURST, EXPECTED_STATUS, ATTEMPTS, SLEEP_MS, PROBE_CALLS);
    }

    @Test
    @DisplayName("1) Deve retornar status de limite após exceder a janela (se houver rate-limit)")
    void shouldReturnTooManyRequestsAfterBurst() throws InterruptedException {

        // --- DETECÇÃO RÁPIDA DE SUPORTE ---
        if (!supportsRateLimitQuickProbe()) {
            Assumptions.assumeTrue(false,
                    "Ambiente não expõe cabeçalhos nem status de rate-limit; teste ignorado rapidamente.");
        }

        // --- CONSUME JANELA PERMITIDA ---
        for (int i = 0; i < BURST; i++) {
            int code = callOnce(PATH).statusCode();
            assertThat(code)
                    .as("Dentro do burst, o código deve ser 2xx (req %s/%s)", i + 1, BURST)
                    .isBetween(200, 299);
            if (SLEEP_MS > 0) Thread.sleep(SLEEP_MS);
        }

        // --- ESTOURA O LIMITE ---
        boolean hitLimit = false;
        int last = -1;

        for (int i = 0; i < ATTEMPTS; i++) {
            last = callOnce(PATH).statusCode();
            if (last == EXPECTED_STATUS) {
                hitLimit = true;
                break;
            }
            if (SLEEP_MS > 0) Thread.sleep(SLEEP_MS);
        }

        assertThat(hitLimit)
                .as("Não recebemos %s após exceder o limite (tentativas=%s, último=%s)",
                        EXPECTED_STATUS, ATTEMPTS, last)
                .isTrue();
    }

    // ---------- Helpers ----------

    private static boolean boolProp(String sys, String env, boolean def) {
        return Boolean.parseBoolean(System.getProperty(sys,
                System.getenv().getOrDefault(env, String.valueOf(def))));
    }
    private static int intProp(String sys, String env, int def) {
        return Integer.parseInt(System.getProperty(sys,
                System.getenv().getOrDefault(env, String.valueOf(def))));
    }
    private static long longProp(String sys, String env, long def) {
        return Long.parseLong(System.getProperty(sys,
                System.getenv().getOrDefault(env, String.valueOf(def))));
    }
    private static String strProp(String sys, String env, String def) {
        return System.getProperty(sys, System.getenv().getOrDefault(env, def));
    }

    /** Faz poucas chamadas para detectar se a API aparenta ter rate-limit.
     *  Critérios:
     *   1) Retornar 429 em alguma das PROBE_CALLS; OU
     *   2) Responder com headers típicos de rate-limit (RateLimit-* ou X-RateLimit-*)
     */
    private boolean supportsRateLimitQuickProbe() {
        for (int i = 0; i < PROBE_CALLS; i++) {
            Response r = callOnce(PATH);
            if (r.statusCode() == EXPECTED_STATUS) return true;
            if (hasRateLimitHeaders(r)) return true;
        }
        return false;
    }

    private boolean hasRateLimitHeaders(Response r) {
        // RFC 9211/RateLimit-* e variantes X-RateLimit-*
        return r.getHeaders().hasHeaderWithName("RateLimit-Limit")
                || r.getHeaders().hasHeaderWithName("RateLimit-Remaining")
                || r.getHeaders().hasHeaderWithName("RateLimit-Reset")
                || r.getHeaders().hasHeaderWithName("X-RateLimit-Limit")
                || r.getHeaders().hasHeaderWithName("X-RateLimit-Remaining")
                || r.getHeaders().hasHeaderWithName("X-RateLimit-Reset")
                || r.getHeaders().hasHeaderWithName("Retry-After");
    }

    @Step("Solicitar GET {path}")
    private Response callOnce(String path) {
        var req = given().filter(new io.qameta.allure.restassured.AllureRestAssured());
        if (TOKEN != null && !TOKEN.isBlank()) {
            req.header(new Header("Authorization", "Bearer " + TOKEN));
        }
        return req.when().get(path).then().extract().response();
    }
}
