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

public class TransferMoneyByUserTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(floats = {9999.99F, 10000F})
    public void userCanTransferCorrectAmountOnOwnAccountTest(float amount) {
        //создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //создание аккаунта
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        //создание второго аккаунта
        CreateAccountResponse createSecondAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        // осуществляем пополнение аккаунта
        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountResponse.getId())
                .balance(5000F)
                .build();

        new DepositAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest);

        DepositAccountResponse depositAccountResponse = new DepositAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(amount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(transferMoneyRequest).extract().as(TransferMoneyResponse.class);

        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");

        // проверка того, что аккаунт пополнился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance() + amount))
                .get(null);

        // проверка того, что баланс на отправляющем аккаунте уменьшился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), Math.round((depositAccountResponse.getBalance() - amount) * 100) / 100.00f))
                .get(null);
    }

    @Test
    public void userCanTransferCorrectAmountOnAccountAnotherUserTest() {
        //создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //создание аккаунта
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        //создание второго пользователя
        CreateUserRequest createDifferentUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createDifferentUserRequest);

        //создание аккаунта у второго пользователя
        CreateAccountResponse createAccountDifferentUserResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createDifferentUserRequest.getUsername(),
                        createDifferentUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        // осуществляем пополнение аккаунта
        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountResponse.getId())
                .balance(1F)
                .build();

        DepositAccountResponse depositAccountResponse = new DepositAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createAccountDifferentUserResponse.getId())
                .amount(0.01F)
                .build();

        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(transferMoneyRequest).extract().as(TransferMoneyResponse.class);

        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");


        // проверка того, что аккаунт пополнился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createDifferentUserRequest.getUsername(),
                        createDifferentUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance() + 0.01F))
                .get(null);

        // проверка того, что баланс на отправляющем аккаунте уменьшился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), depositAccountResponse.getBalance() - 0.01F))
                .get(null);

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
        //создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //создание аккаунта
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        //создание второго аккаунта
        CreateAccountResponse createSecondAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        // осуществляем пополнение аккаунта
        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountResponse.getId())
                .balance(5000F)
                .build();

        new DepositAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest);

        new DepositAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest);

        DepositAccountResponse depositAccountResponse = new DepositAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest).extract().as(DepositAccountResponse.class);

        // переводим сумму
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

        // проверка того, что аккаунт не пополнился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance()))
                .get(null);

        // проверка того, что баланс на отправляющем аккаунте не уменьшился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), depositAccountResponse.getBalance()))
                .get(null);
    }

    @Test
    public void userCanNotTransferAmountMoreThenBalanceTest() {

        //создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //создание аккаунта
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        //создание второго аккаунта
        CreateAccountResponse createSecondAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null).extract().as(CreateAccountResponse.class);

        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(6000F)
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .put(transferMoneyRequest);

        // проверка того, что аккаунт не пополнился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance()))
                .get(null);

        // проверка того, что баланс на отправляющем аккаунте не уменьшился
        new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK(createAccountResponse.getId(), createSecondAccountResponse.getBalance()))
                .get(null);
    }
}
