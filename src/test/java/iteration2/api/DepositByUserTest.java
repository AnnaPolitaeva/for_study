package iteration2.api;

import iteration1.api.BaseTest;
import models.*;
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
        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        // осуществляем пополнение аккаунта
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

        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        // осуществляем пополнение аккаунта
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

        // проверка того, что аккаунт не пополнился
        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance()))
                .get(null);
    }

    @Test
    public void userCanNotDepositDifferentAccountTest() {
        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        UserSteps.createAccount(createUserRequest);

        //создание второго пользователя
        CreateUserRequest createDifferentUserRequest = AdminSteps.createUser().request();

        //создание аккаунта второго пользователя
        CreateAccountResponse createAccountDifferentUserResponse = UserSteps.createAccount(createDifferentUserRequest);

        // осуществляем пополнение аккаунта
        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountDifferentUserResponse.getId())
                .balance(2000F)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsForbidden("Unauthorized access to account"))
                .post(depositAccountRequest);

        // проверка того, что аккаунт не пополнился
        new CrudRequester(
                RequestSpecs.authAsUser(
                        createDifferentUserRequest.getUsername(),
                        createDifferentUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK(createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance()))
                .get(null);
    }
}
