package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
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
import static com.codeborne.selenide.Selenide.$;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyByUserTest {
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

    @Test
    public void userCanTransferMoneyByHisAccountWithCorrectAmountTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер создает два аккаунта
        // ШАГ 4: юзер производит пополнение аккаунта
        // ШАГ 5: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        UserSteps.depositAccount(accountInfo, user, 200F);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги на второй аккаунт
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🆕 New Transfer")).shouldBe(Condition.visible);
        $(".account-selector").selectOptionByValue(String.valueOf(accountInfo.getId()));
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountInfo.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("10");
        //чекбокс
        //кнопка транфер

        // ШАГ 7: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился в UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("✅ Successfully transferred $10 to account " + secondAccountInfo.getAccountNumber() + "!");

        alert.accept();

        $("option[value='" + secondAccountInfo.getId() + "']").shouldHave(text("Balance: $10.00"));
        $("option[value='" + accountInfo.getId() + "']").shouldHave(text("Balance: $190.00"));

        // ШАГ 8: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountRecipient = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(10.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(190.00F, accountSender.getBalance());
    }

    @Test
    public void userCanTransferMoneyByAnotherUserAccountWithCorrectAmountTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает двух юзеров
        // ШАГ 3: юзер 1 создает аккаунт
        // ШАГ 4: юзер 2 создает аккаунт
        // ШАГ 5: юзер 1 производит пополнение аккаунта
        // ШАГ 6: юзер 1 логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        CreateUserRequest anotherUser = AdminSteps.createUser().request();

        String anotherUserAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(anotherUser.getUsername()).password(anotherUser.getPassword()).build())
                .extract()
                .header("Authorization");

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse anotherUserAccountInfo = UserSteps.createAccount(anotherUser);
        UserSteps.depositAccount(accountInfo, user, 200F);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 7: юзер переводит деньги на второй аккаунт
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🆕 New Transfer")).shouldBe(Condition.visible);
        $(".account-selector").selectOptionByValue(String.valueOf(accountInfo.getId()));
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(anotherUserAccountInfo.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("10");
        //чекбокс
        //кнопка транфер

        // ШАГ 8: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился в UI
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("✅ Successfully transferred $10 to account " + anotherUserAccountInfo.getAccountNumber() + "!");
        alert.accept();

        $("option[value='" + accountInfo.getId() + "']").shouldHave(text("Balance: $190.00"));

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", anotherUserAuthHeader);
        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        $(Selectors.byText("💰 Deposit Money")).click();
        $("option[value='" + anotherUserAccountInfo.getId() + "']").shouldHave(text("Balance: $10.00"));

        // ШАГ 9: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse[] existingAnotherUserAccounts = given()
                .spec(RequestSpecs.authAsUser(anotherUser.getUsername(), anotherUser.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountRecipient = Arrays.stream(existingAnotherUserAccounts).filter(account -> account.getAccountNumber().equals(anotherUserAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(10.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(190.00F, accountSender.getBalance());
    }

    @Test
    public void userCanTransferMoneyByHisAccountWithIncorrectAmountTest(){
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
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги на второй аккаунт
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🆕 New Transfer")).shouldBe(Condition.visible);
        $(".account-selector").selectOptionByValue(String.valueOf(accountInfo.getId()));
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountInfo.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("10");
        //чекбокс
        //кнопка транфер

        // ШАГ 7: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился в UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("✅ Successfully transferred $10 to account " + secondAccountInfo.getAccountNumber() + "!");

        alert.accept();

        $("option[value='" + secondAccountInfo.getId() + "']").shouldHave(text("Balance: $10.00"));
        $("option[value='" + accountInfo.getId() + "']").shouldHave(text("Balance: $190.00"));

        // ШАГ 8: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountRecipient = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(10.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(190.00F, accountSender.getBalance());
    }


    @Test
    public void userCanNotTransferMoneyByHisAccountWithoutConfirmTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер создает два аккаунта
        // ШАГ 4: юзер производит пополнение аккаунта
        // ШАГ 5: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        UserSteps.depositAccount(accountInfo, user, 200F);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги на второй аккаунт
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🆕 New Transfer")).shouldBe(Condition.visible);
        $(".account-selector").selectOptionByValue(String.valueOf(accountInfo.getId()));
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(secondAccountInfo.getAccountNumber());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("10");
        //кнопка транфер

        // ШАГ 7: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился в UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("✅ Successfully transferred $10 to account " + secondAccountInfo.getAccountNumber() + "!");

        alert.accept();

        $("option[value='" + secondAccountInfo.getId() + "']").shouldHave(text("Balance: $10.00"));
        $("option[value='" + accountInfo.getId() + "']").shouldHave(text("Balance: $190.00"));

        // ШАГ 8: проверка, что аккаунт-получатель был пополнен, а баланс аккаунта-отправителя уменьшился на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountRecipient = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = Arrays.stream(existingUserAccounts).filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(10.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(190.00F, accountSender.getBalance());
    }
}
