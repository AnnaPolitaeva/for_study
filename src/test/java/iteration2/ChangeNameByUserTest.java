package iteration2;

import generators.RandomData;
import io.qameta.allure.Allure;
import iteration1.BaseTest;
import models.ChangeNameRequest;
import models.CreateUserRequest;
import models.GetInfoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.ChangeNameRequester;
import requests.GetInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeNameByUserTest extends BaseTest {
    private String newName;
    private CreateUserRequest createUserRequest;
    private GetInfoResponse getInfoResponse;

    @Test
    public void userCanChangeNameWithCorrectNameTest() {

        Allure.step("Подготовка тестовых данных", () -> {
            newName = RandomData.getName();
            createUserRequest = createUser();
        });

        Allure.step("Изменение имени пользователя", () -> {
            ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                    .name(newName)
                    .build();

            new ChangeNameRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOKAndMessageSuccess())
                    .post(changeNameRequest);
        });

        Allure.step("Проверка изменений имени пользователя", () -> {
            getInfoResponse = new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .get().extract().as(GetInfoResponse.class);

            softly.assertThat(getInfoResponse.getName()).isEqualTo(newName);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ann 123", "Sara  Parker", "Mike!Shein", "David"})
    public void userCanChangeNameWithIncorrectNameTest(String input) {
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
        });

        Allure.step("Изменение имени пользователя некорректными данными", () -> {
            ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                    .name(input)
                    .build();

            new ChangeNameRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsBadRequestForChangeName())
                    .post(changeNameRequest);
        });

        Allure.step("Проверка, что имя пользователя не изменилось", () -> {
            GetInfoResponse getInfoResponse = new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .get().extract().as(GetInfoResponse.class);

            softly.assertThat(getInfoResponse.getName()).isEqualTo(null);
        });
    }
}
