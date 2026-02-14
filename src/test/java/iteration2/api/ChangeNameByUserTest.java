package iteration2.api;

import api.models.ChangeNameRequest;
import api.models.CreateUserRequest;
import api.generators.RandomData;
import api.models.CreateUserResponse;
import api.models.GetInfoResponse;
import iteration1.api.BaseTest;
import iteration2.ApiAtributesOfResponse;
import models.*;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class ChangeNameByUserTest extends BaseTest {

    @Test
    public void userCanChangeNameWithCorrectNameTest() {

        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(RandomData.getName())
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOKAndMessageSuccess(ApiAtributesOfResponse.MESSAGE_KEY, ApiAtributesOfResponse.PROFILE_UPDATE_SUCCESS))
                .update(changeNameRequest);

        GetInfoResponse getInfoResponse = new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        ModelAssertions.assertThatModels(createUserRequest, getInfoResponse).match();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ann 123", "Sara  Parker", "Mike!Shein", "David"})
    public void userCanChangeNameWithIncorrectNameTest(String input) {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();
        CreateUserResponse createUserResponse = AdminSteps.createUser().response();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(input)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequest(ApiAtributesOfResponse.ERROR_UPDATE_USERNAME))
                .update(changeNameRequest);

        GetInfoResponse getInfoResponse = new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        ModelAssertions.assertThatModels(createUserResponse, getInfoResponse).match();
    }
}
