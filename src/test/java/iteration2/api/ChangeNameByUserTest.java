package iteration2.api;

import iteration1.api.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeNameByUserTest extends BaseTest {

    @Test
    public void UserCanChangeNameWithCorrectNameTest() {

        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //изменение имени
        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name("Bon Jovi")
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK("message", "Profile updated successfully"))
                .update(changeNameRequest);

        // проверка того, что имя установилось
        GetInfoResponse getInfoResponse = new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK())
                .get(null);

        ModelAssertions.assertThatModels(createUserRequest, getInfoResponse).match();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ann 123", "Sara  Parker", "Mike!Shein", "David"})
    public void UserCanChangeNameWithIncorrectNameTest(String input) {
        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();
        CreateUserResponse createUserResponse = AdminSteps.createUser().response();

        //изменение имени
        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(input)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequest("Name must contain two words with letters only"))
                .update(changeNameRequest);

        // проверка того, что имя не установилось
        GetInfoResponse getInfoResponse = new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK())
                .get(null);

        ModelAssertions.assertThatModels(createUserResponse, getInfoResponse).match();
    }
}
