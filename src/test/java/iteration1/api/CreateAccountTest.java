package iteration1.api;

import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest{

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();

        new CrudRequester(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNT,
                ResponseSpecs.entityWasCreated())
                .post();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там

    }
}