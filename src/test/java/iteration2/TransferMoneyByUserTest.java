package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static io.qameta.allure.Allure.step;

public class TransferMoneyByUserTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(floats = {9999.99F, 10000F})
    public void userCanTransferCorrectAmountOnOwnAccountTest(float amount) {
        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);
        CreateAccountResponse createSecondAccountResponse = createAccount(createUserRequest);
        depositAccount(createAccountResponse, 5000F, createUserRequest);
        DepositAccountResponse depositAccountResponse = depositAccount(createAccountResponse, 5000F, createUserRequest);
        TransferMoneyResponse transferMoneyResponse = transferMoneySuccessfully(createAccountResponse, createSecondAccountResponse, amount, createUserRequest);

        step("Step: Check response message", () -> {
            softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");
        });

        checkAccount(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance() + amount);
        checkAccount(createUserRequest, createAccountResponse.getId(), Math.round((depositAccountResponse.getBalance() - amount) * 100) / 100.00f);
    }

    @Test
    public void userCanTransferCorrectAmountOnAccountAnotherUserTest() {
        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);
        CreateUserRequest createDifferentUserRequest = createUser();
        CreateAccountResponse createAccountDifferentUserResponse = createAccount(createDifferentUserRequest);

        DepositAccountResponse depositAccountResponse = depositAccount(createAccountResponse, 1F, createUserRequest);

        TransferMoneyResponse transferMoneyResponse = transferMoneySuccessfully(createAccountResponse, createAccountDifferentUserResponse, 0.01F, createUserRequest);

        step("Step: Check response message", () -> {
            softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");
        });

        checkAccount(createDifferentUserRequest, createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance() + 0.01F);
        checkAccount(createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance() - 0.01F);

    }

    public static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of(10000.01F, "Transfer amount cannot exceed 10000"),
                Arguments.of(-0.01F, "Transfer amount must be at least 0.01"),
                Arguments.of(0F, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    public void userCanNotTransferInvalidAmountTest(float amount, String error) {
        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);
        CreateAccountResponse createSecondAccountResponse = createAccount(createUserRequest);
        depositAccount(createAccountResponse, 5000F, createUserRequest);
        depositAccount(createAccountResponse, 5000F, createUserRequest);
        DepositAccountResponse depositAccountResponse = depositAccount(createAccountResponse, 5000F, createUserRequest);

        transferMoneyWithError(createAccountResponse, createSecondAccountResponse, amount, createUserRequest, error);
        checkAccount(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance());
        checkAccount(createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance());
    }

    @Test
    public void userCanNotTransferAmountMoreThenBalanceTest() {

        CreateUserRequest createUserRequest = createUser();
        CreateAccountResponse createAccountResponse = createAccount(createUserRequest);
        CreateAccountResponse createSecondAccountResponse = createAccount(createUserRequest);

        transferMoneyWithError(createAccountResponse, createSecondAccountResponse, 6000F, createUserRequest, "Invalid transfer: insufficient funds or invalid accounts");
        checkAccount(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance());
        checkAccount(createUserRequest, createAccountResponse.getId(), createSecondAccountResponse.getBalance());
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

    private CreateAccountResponse createAccount(CreateUserRequest createUserRequest) {
        return step("Step: Create account", () -> {
            return new CreateAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.entityWasCreated())
                    .post().extract().as(CreateAccountResponse.class);
        });
    }

    private DepositAccountResponse depositAccount(CreateAccountResponse createAccountResponse, float amount, CreateUserRequest createUserRequest){
        return step("Step: Deposit Account With Correct Amount", () -> {
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
        });
    }

    private TransferMoneyResponse transferMoneySuccessfully(CreateAccountResponse createAccountResponse, CreateAccountResponse createSecondAccountResponse, float amount, CreateUserRequest createUserRequest){
        return step("Step: Transfer Money", () -> {
            TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                    .senderAccountId(createAccountResponse.getId())
                    .receiverAccountId(createSecondAccountResponse.getId())
                    .amount(amount)
                    .build();

            return new TransferMoneyRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .put(transferMoneyRequest).extract().as(TransferMoneyResponse.class);
        });
    }

    private void transferMoneyWithError(CreateAccountResponse createAccountResponse, CreateAccountResponse createSecondAccountResponse, float amount, CreateUserRequest createUserRequest, String error){
        step("Step: Transfer Money", () -> {
            TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                    .senderAccountId(createAccountResponse.getId())
                    .receiverAccountId(createSecondAccountResponse.getId())
                    .amount(amount)
                    .build();

            new TransferMoneyRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsBadRequest(error))
                    .put(transferMoneyRequest);
        });
    }

    private void checkAccount(CreateUserRequest createUserRequest,long accountId, float expectedBalance) {
        step("Step: Check account balance", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(accountId, expectedBalance))
                    .get();
        });
    }
}
