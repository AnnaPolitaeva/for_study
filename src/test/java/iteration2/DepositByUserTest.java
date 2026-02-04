package iteration2;

import generators.RandomData;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositAccountRequester;
import requests.GetInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static io.qameta.allure.Allure.step;

public class DepositByUserTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(floats = {4999.99F, 5000F, 0.01F})
    public void userCanDepositAccountWithCorrectAmountTest(float amount) {

        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);

        DepositAccountResponse depositAccountResponse = depositAccountCorrectAmount(createAccountResponse, amount, createUserRequest);

        step("Step: Check account balance", () -> {
            softly.assertThat(createAccountResponse.getBalance() + amount).isEqualTo(depositAccountResponse.getBalance());
        });


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
    @Description("User Can Not Deposit Account With Invalid Amount")
    public void userCanNotDepositAccountWithInvalidAmountTest(float amount, String error) {
        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);
        depositAccountIncorrectAmount(createAccountResponse, amount, createUserRequest, error);
        checkAccount(createUserRequest, createAccountResponse);
    }

    @Test
    public void userCanNotDepositDifferentAccountTest() {
        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);
        float amount = RandomData.getAmount();

        CreateUserRequest createDifferentUserRequest = createUser();
        CreateAccountResponse createAccountDifferentUserResponse = createAccount(createDifferentUserRequest);
        depositAccountWithUnauthorized(createAccountDifferentUserResponse, amount, createUserRequest);
        checkAccount(createDifferentUserRequest, createAccountDifferentUserResponse);

    }


    @Step ("Deposit Account")
    private DepositAccountResponse depositAccountCorrectAmount(CreateAccountResponse createAccountResponse, float amount, CreateUserRequest createUserRequest){
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(amount)
                    .build();

            return new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);
    }

    @Step ("Deposit Account")
    private void depositAccountIncorrectAmount(CreateAccountResponse createAccountResponse, float amount, CreateUserRequest createUserRequest, String error){
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(amount)
                    .build();

            new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsBadRequest(error))
                    .post(depositAccountRequest);
    }

    @Step ("Deposit Account")
    private void depositAccountWithUnauthorized(CreateAccountResponse createAccountResponse, float amount, CreateUserRequest createUserRequest){
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(amount)
                    .build();

            new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsForbidden())
                    .post(depositAccountRequest);
    }

    @Step ("Check Account Balance")
    private void checkAccount(CreateUserRequest createUserRequest, CreateAccountResponse createAccountResponse) {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance()))
                    .get();
    }
}
