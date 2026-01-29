package iteration2.ui;

import com.codeborne.selenide.*;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositByUserTest {
    @BeforeAll
    public static void setupSelenoid(){
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://10.8.0.19:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enebleLog", true)
        );
    }
    //Ann12345
    //UZTvfcr831%%
    @Test
    public void userCanDepositAccountWithCorrectAmount(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер создает два аккаунта
        // ШАГ 4: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер производит пополнение аккаунта
        $(Selectors.byText("💰 Deposit Money")).click();
        $(".account-selector").selectOptionByValue(String.valueOf(accountInfo.getId()));
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("200");
        $(Selectors.byText("💵 Deposit")).click();

        // ШАГ 6: проверка, что аккаунт пополнен на UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("✅ Successfully deposited $200 to account " + accountInfo.getAccountNumber() + "!");

        alert.accept();

        $(Selectors.byText("💰 Deposit Money")).click();

        $("option[value='" + accountInfo.getId() + "']").shouldHave(text("Balance: $200.00"));

        // ШАГ 7: проверка, что аккаунт был пополнен на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse createdAccount = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(createdAccount).isNotNull();
        assertEquals(200.00F, createdAccount.getBalance());
    }

    @Test
    public void userCanDepositAccountWithIncorrectAmount(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер создает аккаунт
        // ШАГ 4: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер производит пополнение аккаунта
        $(Selectors.byText("💰 Deposit Money")).click();
        $(".account-selector").selectOptionByValue(String.valueOf(accountInfo.getId()));
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("0");
        $(Selectors.byText("💵 Deposit")).click();

        // ШАГ 6: проверка, что аккаунт НЕ пополнен на UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("❌ Please enter a valid amount.");

        alert.accept();


        $("option[value='" + accountInfo.getId() + "']").shouldHave(text("Balance: $0.00"));

        // ШАГ 7: проверка, что аккаунт НЕ был пополнен на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse createdAccount = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(createdAccount).isNotNull();
        assertEquals(0.00F, createdAccount.getBalance());
    }
}
