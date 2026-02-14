package iteration1.api;

import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class LoginUserTest extends BaseTest{
    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username(Config.getProperty("admin.username"))
                .password(Config.getProperty("admin.password"))
                .build();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest user = AdminSteps.createUser().request();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());
    }
}