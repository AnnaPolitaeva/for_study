package iteration2.api;

import generators.RandomData;
import api.models.*;
import iteration1.api.BaseTest;
import iteration2.ApiAtributesOfResponse;
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
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        CreateAccountResponse createSecondAccountResponse = UserSteps.createAccount(createUserRequest);

        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(createAccountResponse, createUserRequest, RandomData.getBigAmount());

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

        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ApiAtributesOfResponse.TRANSFER_SUCCESS);

        UserSteps.checkBalancesAfterTransfer(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance() + amount,
                createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance() - amount);
    }

    @Test
    public void userCanTransferCorrectAmountOnAccountAnotherUserTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        CreateUserRequest createDifferentUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountDifferentUserResponse = UserSteps.createAccount(createDifferentUserRequest);

        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(createAccountResponse, createUserRequest, RandomData.getBigAmount());

        float transferAmount = RandomData.getSmallAmount();
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createAccountDifferentUserResponse.getId())
                .amount(transferAmount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOKAndMessageSuccess(ApiAtributesOfResponse.MESSAGE_KEY, ApiAtributesOfResponse.TRANSFER_SUCCESS))
                .post(transferMoneyRequest);

        UserSteps.checkBalancesAfterTransfer(createDifferentUserRequest, createAccountDifferentUserResponse.getId(), createAccountDifferentUserResponse.getBalance() + transferAmount,
                createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance() - transferAmount);
    }

    public static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of(RandomData.getBigAmount(), 10000.01F, "Transfer amount cannot exceed 10000"),
                Arguments.of(RandomData.getSmallAmount(), -0.01F, "Transfer amount must be at least 0.01"),
                Arguments.of(RandomData.getSmallAmount(), 0F, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    public void userCanNotTransferInvalidAmountTest(float depositAmount, float amount, String error) {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        CreateAccountResponse createSecondAccountResponse = UserSteps.createAccount(createUserRequest);

        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(createAccountResponse, createUserRequest, depositAmount);

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

        UserSteps.checkBalancesAfterTransfer(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance(),
                createUserRequest, createAccountResponse.getId(), depositAccountResponse.getBalance());
    }

    @Test
    public void userCanNotTransferAmountMoreThenBalanceTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser().request();

        CreateAccountResponse createAccountResponse = UserSteps.createAccount(createUserRequest);

        CreateAccountResponse createSecondAccountResponse = UserSteps.createAccount(createUserRequest);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createAccountResponse.getId())
                .receiverAccountId(createSecondAccountResponse.getId())
                .amount(RandomData.getSmallAmount())
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(ApiAtributesOfResponse.ERROR_TRANSFER))
                .post(transferMoneyRequest);

        UserSteps.checkBalancesAfterTransfer(createUserRequest, createSecondAccountResponse.getId(), createSecondAccountResponse.getBalance(),
                createUserRequest, createAccountResponse.getId(), createSecondAccountResponse.getBalance());
    }
}
