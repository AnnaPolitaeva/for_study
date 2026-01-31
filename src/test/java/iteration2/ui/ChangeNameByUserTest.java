package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.GetInfoResponse;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeNameByUserTest {
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
    public void userCanChangeNameWithCorrectNameTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер меняет имя
        $(Selectors.byText("Noname")).click();
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible)
                .setValue("Bon Jovi")
                .shouldHave(Condition.value("Bon Jovi"));
        $(Selectors.byText("💾 Save Changes")).click();

        // ШАГ 5: проверка, что имя было изменено в UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("✅ Name updated successfully!");

        alert.accept();

        Selenide.refresh();

        $(Selectors.byText(user.getUsername())).parent().shouldHave(text("Bon Jovi"));

        // ШАГ 6: проверка, что имя было изменено на API
        GetInfoResponse userInfo = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .extract().as(GetInfoResponse.class);

        assertThat(userInfo.getName()).isNotNull();
        assertEquals("Bon Jovi", userInfo.getName());
    }

    @Test
    public void userCanChangeNameWithIncorrectNameTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер меняет имя
        $(Selectors.byText("Noname")).click();
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible)
                .setValue("David")
                .shouldHave(Condition.value("David"));
        $(Selectors.byText("💾 Save Changes")).click();

        // ШАГ 5: проверка, что имя было изменено в UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("Name must contain two words with letters only");

        alert.accept();

        Selenide.refresh();

        $(Selectors.byText(user.getUsername())).parent().shouldHave(text("Noname"));

        // ШАГ 6: проверка, что имя было изменено на API
        GetInfoResponse userInfo = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .extract().as(GetInfoResponse.class);

        assertThat(userInfo.getName()).isNull();
    }

    @Test
    public void userCanChangeNameWithEmptyNameTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser().request();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер меняет имя
        $(Selectors.byText("Noname")).click();
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byText("💾 Save Changes")).click();

        // ШАГ 5: проверка, что имя было изменено в UI
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("❌ Please enter a valid name.");

        alert.accept();

        Selenide.refresh();

        $(Selectors.byText(user.getUsername())).parent().shouldHave(text("Noname"));

        // ШАГ 6: проверка, что имя было изменено на API
        GetInfoResponse userInfo = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .extract().as(GetInfoResponse.class);

        assertThat(userInfo.getName()).isNull();
    }
}
