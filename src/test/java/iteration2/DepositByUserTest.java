package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DepositByUserTest {
    static String userAuthHeader;
    static String differentUserAuthHeader;
    static int accountId;
    static int diferentAccountId;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));

        // создание пользователя
//        given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
//                .body("""
//                        {
//                          "username": "Ann12345",
//                          "password": "Ann12345!",
//                          "role": "USER"
//                        }
//                        """)
//                .post("http://localhost:4111/api/v1/admin/users")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "Ann12345",
                          "password": "Ann12345!"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет) и забираем его id
        accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        // создание другого пользователя
//        given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
//                .body("""
//                        {
//                          "username": "Ann54321",
//                          "password": "Ann54321!",
//                          "role": "USER"
//                        }
//                        """)
//                .post("http://localhost:4111/api/v1/admin/users")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен другого юзера
        differentUserAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "Ann54321",
                          "password": "Ann54321!"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет) у другого юзера и забираем его id
        diferentAccountId = given()
                .header("Authorization", differentUserAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");
    }

    @ParameterizedTest
    @ValueSource(floats = {4999.99F, 5000F, 0.01F})
    public void userCanDepositAccountWithCorrectAmountTest(float amount) {
        String json = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """, accountId, amount);

        // сначала смотрим сколько на балансе
        float balance = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // осуществляем пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // проверка того, что аккаунт пополнился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(balance + amount));
    }

    public static Stream<Arguments> invalidData(){
        return Stream.of(
                Arguments.of(-3000, "Deposit amount must be at least 0.01"),
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    public void userCanDepositAccountWithInvalidAmountTest(double amount, String error) {
        String json = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %.2f
                }
                """, accountId, amount);

        // сначала смотрим сколько на балансе
        float balance = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // осуществляем пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo(error));

        // проверка того, что аккаунт не пополнился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(balance));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", "сто"})
    public void userCanDepositAccountWithoutAmountTest(String amount) {
        String json = String.format("""
                {
                  "id": %d,
                  "balance": %s
                }
                """, accountId, amount);

        // сначала смотрим сколько на балансе
        float balance = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // осуществляем пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // проверка того, что аккаунт не пополнился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(balance));
    }

    @Test
    public void userCanNotDepositDifferentAccountTest() {
        String json = String.format("""
                {
                  "id": %d,
                  "balance": 2000
                }
                """, diferentAccountId);

        // сначала смотрим сколько на балансе
        float balance = given()
                .header("Authorization", differentUserAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", diferentAccountId));

        // осуществляем пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(equalTo("Unauthorized access to account"));

        // проверка того, что аккаунт не пополнился
        given()
                .header("Authorization", differentUserAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", diferentAccountId), equalTo(balance));
    }
}
