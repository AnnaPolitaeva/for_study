package api.requests.steps;

import api.models.*;
import io.restassured.response.ValidatableResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public UserSteps() {
    }

    public static CreateAccountResponse createAccount(CreateUserRequest createUserRequest){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT,
                ResponseSpecs.entityWasCreated())
                .post();
    }

    public static DepositAccountResponse depositAccount(CreateAccountResponse createAccountResponse, CreateUserRequest createUserRequest, float amount){
        float MAX_AMOUNT_FOR_DEPOSIT = 5000F;

        int n = (int) Math.ceil(amount / MAX_AMOUNT_FOR_DEPOSIT);

        for (int i = 1; i < n; i++){
            DepositAccountRequest depositAccountMaxRequest = DepositAccountRequest.builder()
                    .id(createAccountResponse.getId())
                    .balance(MAX_AMOUNT_FOR_DEPOSIT)
                    .build();

            new CrudRequester(
                    RequestSpecs.authAsUser(
                            createUserRequest.getUsername(),
                            createUserRequest.getPassword()),
                    Endpoint.ACCOUNT_DEPOSIT,
                    ResponseSpecs.requestReturnsOK())
                .post(depositAccountMaxRequest);

            amount = amount - MAX_AMOUNT_FOR_DEPOSIT;
            if (amount == MAX_AMOUNT_FOR_DEPOSIT){
                break;
            }
        }

        DepositAccountRequest depositAccountRequest = DepositAccountRequest.builder()
                .id(createAccountResponse.getId())
                .balance(amount)
                .build();

        return new ValidatedCrudRequester<DepositAccountResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositAccountRequest);
    }

    public static void checkBalance(CreateUserRequest createUserRequest, long accountId, float expectedBalance) {
        new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK(accountId, Math.round((expectedBalance) * 100) / 100.00f))
                .get();
    }

    public static void checkBalancesAfterTransfer(CreateUserRequest createUserRequest1, long accountId1, float expectedBalance1,CreateUserRequest createUserRequest2, long accountId2, float expectedBalance2) {
        checkBalance(createUserRequest1, accountId1, expectedBalance1);
        checkBalance(createUserRequest2, accountId2, expectedBalance2);
    }

    public List<CreateAccountResponse> getAllAccounts(){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);
    }

    public GetInfoResponse getUserInfo(){
        return new ValidatedCrudRequester<GetInfoResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()).get();
    }
}
