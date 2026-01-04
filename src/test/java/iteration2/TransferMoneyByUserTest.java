package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static io.restassured.RestAssured.given;

public class TransferMoneyByUserTest {
    static String userAuthHeader;
    static String differentUserAuthHeader;
    static int accountId;
    static int diferentAccountId;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));

//        // создание пользователя
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

//        // создание другого пользователя
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
}
