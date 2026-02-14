package iteration1;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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