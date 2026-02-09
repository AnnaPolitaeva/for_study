package iteration2;

import generators.RandomData;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import iteration1.BaseTest;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.DepositAccountRequest;
import models.DepositAccountResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.DepositAccountRequester;
import requests.GetInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositByUserTest extends BaseTest {
    private CreateUserRequest createUserRequest;
    private CreateUserRequest createDifferentUserRequest;
    private CreateAccountResponse createAccountResponse;
    private CreateAccountResponse createAccountDifferentUserResponse;
    private DepositAccountResponse depositAccountResponse;
    private float amount;


    @ParameterizedTest
    @ValueSource(floats = {4999.99F, 5000F, 0.01F})
    public void userCanDepositAccountWithCorrectAmountTest(float amount) {
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            createAccountResponse = createAccount(createUserRequest);
        });

        Allure.step("Пополнение аккаунта пользователя корректной суммой", () -> {
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(amount)
                    .build();

            depositAccountResponse = new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

            softly.assertThat(createAccountResponse.getBalance() + amount).isEqualTo(depositAccountResponse.getBalance());
        });

        Allure.step("Проверка изменений баланса аккаунта пользователя", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance() + amount))
                    .get();
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
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            createAccountResponse = createAccount(createUserRequest);
        });

        Allure.step("Пополнение аккаунта пользователя НЕкорректной суммой", () -> {
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
        });

        Allure.step("Проверка, что баланс аккаунта пользователя не изменился", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createAccountResponse.getBalance()))
                    .get();
        });
    }

    @Test
    public void userCanNotDepositDifferentAccountTest() {
        Allure.step("Подготовка тестовых данных", () -> {
            createUserRequest = createUser();
            amount = RandomData.getAmount();
            createDifferentUserRequest = createUser();
            createAccountDifferentUserResponse = createAccount(createDifferentUserRequest);
        });

        Allure.step("Пополнение аккаунта другого пользователя", () -> {
            DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                    .id(createAccountDifferentUserResponse.getId())
                    .balance(amount)
                    .build();

            new DepositAccountRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsForbidden())
                    .post(depositAccountRequest);
        });

        Allure.step("Проверка, что баланс аккаунта пользователя не изменился", () -> {
            new GetInfoRequester(
                    RequestSpecs.authAsUser(
                            createDifferentUserRequest.getUsername(),
                            createDifferentUserRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK(createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance()))
                    .get();
        });
    }
}
