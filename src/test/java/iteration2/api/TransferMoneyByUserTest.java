package iteration2.api;

import api.models.*;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferMoneyByUserTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(floats = {9999.99F, 10000F})
    public void userCanTransferCorrectAmountOnOwnAccountTest(float amount) {
        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        //создание второго аккаунта
        CreateAccountResponse createSecondAccountResponse = UserSteps.createAccount(createUserRequest);

        // осуществляем пополнение аккаунта
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(createAccountResponse, createUserRequest, 10000F);

        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(amount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transferMoneyRequest);

        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo("Transfer successful");

        // проверка того, что аккаунт пополнился
        UserSteps.checkBalance(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance() + amount);

        // проверка того, что баланс на отправляющем аккаунте уменьшился
        UserSteps.checkBalance(createUserRequest, createAccountResponse.getId(), Math.round((depositAccountResponse.getBalance() - amount) * 100) / 100.00f);
    }

    @Test
    public void userCanTransferCorrectAmountOnAccountAnotherUserTest() {
        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        //создание второго пользователя
        CreateUserRequest createDifferentUserRequest = AdminSteps.createUser().request();

        //создание аккаунта у второго пользователя
        CreateAccountResponse createAccountDifferentUserResponse = UserSteps.createAccount(createDifferentUserRequest);

        // осуществляем пополнение аккаунта
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(createAccountResponse, createUserRequest, 1F);

        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createAccountDifferentUserResponse.getId())
                .amount(0.01F)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOK("message", "Transfer successful"))
                .post(transferMoneyRequest);

        // проверка того, что аккаунт пополнился
        UserSteps.checkBalance(createDifferentUserRequest, createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance() + 0.01F);

        // проверка того, что баланс на отправляющем аккаунте уменьшился
        UserSteps.checkBalance(createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance() - 0.01F);
    }

    public static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of(12000F, 10000.01F, "Transfer amount cannot exceed 10000"),
                Arguments.of(1F, -0.01F, "Transfer amount must be at least 0.01"),
                Arguments.of(1F, 0F, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    public void userCanNotTransferInvalidAmountTest(float depositAmount, float amount, String error) {
        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        //создание второго аккаунта
        CreateAccountResponse createSecondAccountResponse = UserSteps.createAccount(createUserRequest);

        // осуществляем пополнение аккаунта
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(createAccountResponse, createUserRequest, depositAmount);


        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(error))
                .post(transferMoneyRequest);

        // проверка того, что аккаунт не пополнился
        UserSteps.checkBalance(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance());

        // проверка того, что баланс на отправляющем аккаунте не уменьшился
        UserSteps.checkBalance(createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance());
    }

    @Test
    public void userCanNotTransferAmountMoreThenBalanceTest() {

        //создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        //создание аккаунта
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        //создание второго аккаунта
        CreateAccountResponse createSecondAccountResponse = UserSteps.createAccount(createUserRequest);

        // переводим сумму
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(6000F)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferMoneyRequest);

        // проверка того, что аккаунт не пополнился
        UserSteps.checkBalance(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance());

        // проверка того, что баланс на отправляющем аккаунте не уменьшился
        UserSteps.checkBalance(createUserRequest, createAccountResponse.getId(), createSecondAccountResponse.getBalance());
    }
}
