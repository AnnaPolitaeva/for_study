package iteration1;

import generators.RandomData;
import io.qameta.allure.Step;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.UserRole;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        this.softly.assertAll();
    }


    @Step("Create User")
    protected CreateUserRequest createUser(){
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
    }

    @Step ("Create Account")
    protected CreateAccountResponse createAccount(CreateUserRequest createUserRequest) {
        return new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);
    }
}
