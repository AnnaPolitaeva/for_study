package iteration2;

import generators.RandomData;
import io.qameta.allure.Allure;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.DepositAccountRequester;
import requests.GetInfoRequester;
import requests.TransferMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferMoneyByUserTest extends BaseTest {
    private CreateUserRequest createUserRequest;
    private CreateAccountResponse createAccountResponse;
    private CreateUserRequest createDifferentUserRequest;
    private CreateAccountResponse createAccountDifferentUserResponse;
    private CreateAccountResponse createSecondAccountResponse;
    private DepositAccountResponse depositAccountResponse;
    private TransferMoneyResponse transferMoneyResponse;
    private float amount;

    @ParameterizedTest
    @ValueSource(floats = {9999.99F, 10000F})
    public void userCanTransferCorrectAmountOnOwnAccountTest(float amount) {
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            createAccountResponse = createAccount(createUserRequest);
            createSecondAccountResponse = createAccount(createUserRequest);
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(5000F)
                    .build();

            new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

            depositAccountResponse = new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);
        });

        Allure.step("Перевод корректной суммы с одного аккаунта на другой", () -> {
            TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                    .senderAccountId(createAccountResponse.getId())
                    .receiverAccountId(createSecondAccountResponse.getId())
                    .amount(amount)
                    .build();

            transferMoneyResponse = new TransferMoneyRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .put(transferMoneyRequest).extract().as(TransferMoneyResponse.class);

            softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");
        });

        Allure.step("Проверка изменений баланса аккаунта-отправителя", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), Math.round((depositAccountResponse.getBalance() - amount) * 100) / 100.00f))
                    .get();
        });

        Allure.step("Проверка изменений баланса аккаунта-получателя", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance() + amount))
                    .get();
        });
    }

    @Test
    public void userCanTransferCorrectAmountOnAccountAnotherUserTest() {
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            createAccountResponse = createAccount(createUserRequest);
            createDifferentUserRequest = createUser();
            createAccountDifferentUserResponse = createAccount(createDifferentUserRequest);

            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(1F)
                    .build();

            depositAccountResponse = new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);
        });

        Allure.step("Перевод корректной суммы с одного аккаунта на другой", () -> {
            TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                    .senderAccountId(createAccountResponse.getId())
                    .receiverAccountId(createAccountDifferentUserResponse.getId())
                    .amount(0.01F)
                    .build();

            transferMoneyResponse = new TransferMoneyRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .put(transferMoneyRequest).extract().as(TransferMoneyResponse.class);

            softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");
        });

        Allure.step("Проверка изменений баланса аккаунта-отправителя", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), depositAccountResponse.getBalance() - 0.01F))
                    .get();
        });

        Allure.step("Проверка изменений баланса аккаунта-получателя", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createDifferentUserRequest.getUsername(),
                            createDifferentUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance() + 0.01F))
                    .get();
        });
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
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            createAccountResponse = createAccount(createUserRequest);
            createSecondAccountResponse = createAccount(createUserRequest);
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(5000F)
                    .build();

            new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

            new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

            depositAccountResponse = new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);
        });

        Allure.step("Перевод НЕкорректной суммы", () -> {
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

        Allure.step("Проверка, что баланс аккаунта-отправителя не изменился", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance()))
                    .get();
        });

        Allure.step("Проверка, что баланс аккаунта-получателя не изменился", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance()))
                    .get();
        });
    }

    @Test
    public void userCanNotTransferAmountMoreThenBalanceTest() {

        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            createAccountResponse = createAccount(createUserRequest);
            createSecondAccountResponse = createAccount(createUserRequest);
            amount = RandomData.getAmount();
        });

        Allure.step("Перевод корректной суммы с аккаунта с нулевым балансом", () -> {
            TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                    .senderAccountId(createAccountResponse.getId())
                    .receiverAccountId(createSecondAccountResponse.getId())
                    .amount(amount)
                    .build();

            new TransferMoneyRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsBadRequestInTransfer())
                    .put(transferMoneyRequest);
        });

        Allure.step("Проверка, что баланс аккаунта-отправителя не изменился", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance()))
                    .get();
        });

        Allure.step("Проверка, что баланс аккаунта-получателя не изменился", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance()))
                    .get();
        });
    }
}
