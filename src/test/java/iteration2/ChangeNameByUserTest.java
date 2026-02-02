package iteration2;

import generators.RandomData;
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

    private CreateUserRequest createUser(){
        return step("Step: Create user", () -> {
            CreateUserRequest createUserRequest = CreateUserRequest.builder()
                    .username(RandomData.getUsername())
                    .password(RandomData.getPassword())
                    .role(UserRole.USER.toString())
                    .build();

            new AdminCreateUserRequester(
                    RequestSpecs.adminSpec(),
                    ResponseSpecs.entityWasCreated())
                    .post(createUserRequest);
            return createUserRequest;
        });
    }

    private void changeNameCorrect(CreateUserRequest createUserRequest, String name){
        step("Step: Change Name With Correct value", () -> {
            ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                    .name(name)
                    .build();

            new ChangeNameRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOKAndMessageSuccess())
                    .post(changeNameRequest);
        });
    }

    private void changeNameIncorrect(CreateUserRequest createUserRequest, String name){
        step("Step: Change Name With Incorrect value", () -> {
            ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                    .name(name)
                    .build();

            new ChangeNameRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsBadRequestForChangeName())
                    .post(changeNameRequest);
        });
    }

    private void checkName(CreateUserRequest createUserRequest, String expectedName){
        step("Step: Change Name With Incorrect value", () -> {
            GetInfoResponse getInfoResponse = new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .get().extract().as(GetInfoResponse.class);

            softly.assertThat(getInfoResponse.getName()).isEqualTo(expectedName);
        });
    }
}
