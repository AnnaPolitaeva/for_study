package iteration2.ui;

import api.models.CreateUserRequest;
import api.models.GetInfoResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import iteration1.ui.BaseUiTest;
import generators.RandomData;
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

public class ChangeNameByUserTest extends BaseUiTest {
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


        Selenide.refresh();
        String newName = RandomData.getName();
        new EditProfile().checkUsername(user.getUsername(), newName);

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNotNull();
        assertEquals(newName, userInfo.getName());
    }

    @Test
    public void userCanChangeNameWithIncorrectNameTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName("David").checkAlertMessageAdnAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Noname");

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNull();
    }

    @Test
    public void userCanChangeNameWithEmptyNameTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName(null).checkAlertMessageAdnAccept(BankAlert.PLEASE_ENTER_A_VALID_NAME.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Noname");

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNull();
    }
}
