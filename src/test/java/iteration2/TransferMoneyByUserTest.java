package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
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

public class TransferMoneyByUserTest {
    static String userAuthHeader;
    static String differentUserAuthHeader;
    static int accountId;
    static int secondAccountId;
    static int differentAccountId;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));

        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Ann12345",
                          "password": "Ann12345!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

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

        // создаем первый аккаунт(счет) и забираем его id
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

        // создаем второй аккаунт(счет) и забираем его id
        secondAccountId = given()
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
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Ann54321",
                          "password": "Ann54321!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

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
        differentAccountId = given()
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
    @ValueSource(floats = {9999.99F, 10000F})
    public void userCanTransferCorrectAmountOnOwnAccountTest(float amount) {
        String json1 = String.format("""
                {
                  "id": %d,
                  "balance": 5000.00
                }
                """, accountId);
        //пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json1)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json1)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);


        // сначала смотрим сколько на балансе отправляющего аккаунта
        float balance1 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // сначала смотрим сколько на балансе получающего аккаунта
        float balance2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", secondAccountId));


        String json2 = String.format(Locale.US, """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %.2f
                }
                """, accountId, secondAccountId, amount);

        // осуществляем перевод суммы
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json2)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);


        // проверка того, что баланс отправляющего аккаунта изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(Math.round((balance1 - amount) * 100) / 100.0f));

        // проверка того, что баланс получающего аккаунта изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", secondAccountId), equalTo(Math.round((balance2 + amount) * 100) / 100.0f));
    }

    @Test
    public void userCanTransferCorrectAmountOnAccountAnotherUserTest() {
        String json1 = String.format("""
                {
                  "id": %d,
                  "balance": 1
                }
                """, accountId);
        //пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json1)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // сначала смотрим сколько на балансе отправляющего аккаунта
        float balance1 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // сначала смотрим сколько на балансе получающего аккаунта
        float balance2 = given()
                .header("Authorization", differentUserAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", differentAccountId));

        String json = String.format("""
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": 0.01
                }
                """, accountId, differentAccountId);

        // осуществляем перевод суммы
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // проверка того, что баланс отправляющего аккаунта изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(balance1 - 0.01F));

        // проверка того, что баланс получающего аккаунта изменился
        given()
                .header("Authorization", differentUserAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", differentAccountId), equalTo(balance2 + 0.01F));
    }

    public static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"),
                Arguments.of(0, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    public void userCanNotTransferInvalidAmountTest(double amount, String error) {
        String json1 = String.format("""
                {
                  "id": %d,
                  "balance": 5000
                }
                """, accountId);
        //пополнение аккаунта
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json1)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json1)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json1)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // сначала смотрим сколько на балансе отправляющего аккаунта
        float balance1 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // сначала смотрим сколько на балансе получающего аккаунта
        float balance2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", secondAccountId));


        String json = String.format(Locale.US, """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %.2f
                }
                """, accountId, secondAccountId, amount);

        // осуществляем перевод суммы
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo(error));

        // проверка того, что баланс отправляющего аккаунта не изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(balance1));

        // проверка того, что баланс получающего аккаунта не изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", secondAccountId), equalTo(balance2));
    }

    @Test
    public void userCanNotTransferAmountMoreThenBalanceTest() {

        // сначала смотрим сколько на балансе получающего аккаунта
        float balance = given()
                .header("Authorization", differentUserAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", differentAccountId));

        //создаем новый аккаунт, чтоб на нем был нулевой баланс и вытягиваем его id
        int newAccountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String json = String.format("""
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": 6000
                }
                """, newAccountId, differentAccountId);

        // осуществляем перевод суммы
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        // проверка того, что баланс отправляющего аккаунта не изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(0.0F));

        // проверка того, что баланс получающего аккаунта не изменился
        given()
                .header("Authorization", differentUserAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", differentAccountId), equalTo(balance));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", "сто"})
    public void userCanNotTransferEmptyAmountOrStringAmountTest(String amount) {

        // сначала смотрим сколько на балансе отправляющего аккаунта
        float balance1 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", accountId));

        // сначала смотрим сколько на балансе получающего аккаунта
        float balance2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path(String.format("accounts.find { it.id == %d }.balance", secondAccountId));

        String json = String.format("""
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %s
                }
                """, accountId, secondAccountId, amount);

        // осуществляем перевод суммы
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // проверка того, что баланс отправляющего аккаунта не изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", accountId), equalTo(balance1));

        // проверка того, что баланс получающего аккаунта не изменился
        given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(String.format("accounts.find { it.id == %d }.balance", secondAccountId), equalTo(balance2));
    }
}
