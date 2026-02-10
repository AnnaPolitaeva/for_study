package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositByUserTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(floats = {4999.99F, 5000F, 0.01F})
    public void userCanDepositAccountWithCorrectAmountTest(float amount) {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountResponse.getId())
                .balance(amount)
                .build();

        DepositAccountResponse depositAccountResponse = new ValidatedCrudRequester<DepositAccountResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest);

        softly.assertThat(createAccountResponse.getBalance() + amount).isEqualTo(depositAccountResponse.getBalance());

        GetInfoResponse getInfoResponse = new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        ModelAssertions.assertThatModels(depositAccountRequest, getInfoResponse);
    }

    public static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of(-3000F, "Deposit amount must be at least 0.01"),
                Arguments.of(-0.01F, "Deposit amount must be at least 0.01"),
                Arguments.of(0F, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01F, "Deposit amount cannot exceed 5000")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    public void userCanDepositAccountWithInvalidAmountTest(float amount, String error) {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountResponse.getId())
                .balance(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(error))
                .post(depositAccountRequest);

        GetInfoResponse getInfoResponse = new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance()))
                .get();

        ModelAssertions.assertThatModels(createAccountResponse, getInfoResponse);
    }

    @Test
    public void userCanNotDepositDifferentAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        UserSteps.createAccount(createUserRequest);

        CreateUserRequest createDifferentUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountDifferentUserResponse = UserSteps.createAccount(createDifferentUserRequest);

        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountDifferentUserResponse.getId())
                .balance(RandomData.getAmount())
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(ApiAtributesOfResponse.ERROR_UNAUTHORISED))
                .post(depositAccountRequest);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createDifferentUserRequest.getUsername(),
                        createDifferentUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK(createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance()))
                .get();
    }
}
