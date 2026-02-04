package iteration2;

import generators.RandomData;
import io.qameta.allure.Step;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import requests.GetInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.qameta.allure.Allure.step;

public class ChangeNameByUserTest extends BaseTest {

    @Test
    public void userCanChangeNameWithCorrectNameTest() {
        String newName = RandomData.getName();
        CreateUserRequest createUserRequest = createUser();
        changeNameCorrect(createUserRequest, newName);
        checkName(createUserRequest, newName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ann 123", "Sara  Parker", "Mike!Shein", "David"})
    public void userCanChangeNameWithIncorrectNameTest(String input) {
        CreateUserRequest createUserRequest = createUser();
        changeNameIncorrect(createUserRequest, input);
        checkName(createUserRequest, null);
    }

    @Step("Change Name With Correct value")
    private void changeNameCorrect(CreateUserRequest createUserRequest, String name){
            ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                    .name(name)
                    .build();

            new ChangeNameRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOKAndMessageSuccess())
                    .post(changeNameRequest);
    }

    @Step("Change Name With Incorrect value")
    private void changeNameIncorrect(CreateUserRequest createUserRequest, String name){
            ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                    .name(name)
                    .build();

            new ChangeNameRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsBadRequestForChangeName())
                    .post(changeNameRequest);
    }

    @Step("Check Name")
    private void checkName(CreateUserRequest createUserRequest, String expectedName){
            GetInfoResponse getInfoResponse = new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .get().extract().as(GetInfoResponse.class);

            softly.assertThat(getInfoResponse.getName()).isEqualTo(expectedName);
    }
}
