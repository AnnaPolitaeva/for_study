package requests.steps;

import io.restassured.response.ValidatableResponse;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.DepositAccountRequest;
import models.DepositAccountResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UserSteps {

    public static CreateAccountResponse createAccount(CreateUserRequest createUserRequest){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.ACCOUNT,
                ResponseSpecs.entityWasCreated())
                .post(null);
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

    public static ValidatableResponse checkBalance(CreateUserRequest createUserRequest, long accountId, float expectedBalance) {
        return new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK(accountId, Math.round((expectedBalance) * 100) / 100.00f))
                .get();
    }
}
