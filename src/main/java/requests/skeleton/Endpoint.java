package requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    LOGIN("/auth/login", LoginUserRequest.class, LoginUserResponse.class),
    ADMIN_USER("/admin/users", CreateUserRequest.class, CreateUserResponse.class),
    ACCOUNT("/accounts", BaseModel.class, CreateAccountResponse.class),
    CUSTOMER_PROFILE("/customer/profile", ChangeNameRequest.class, GetInfoResponse.class),
    CUSTOMER_PROFILE_GET("/customer/profile", BaseModel.class, GetInfoResponse.class),
    ACCOUNT_DEPOSIT("/accounts/deposit", DepositAccountRequest.class, DepositAccountResponse.class),
    ACCOUNT_TRANSFER("/accounts/transfer", TransferMoneyRequest.class, TransferMoneyResponse.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
