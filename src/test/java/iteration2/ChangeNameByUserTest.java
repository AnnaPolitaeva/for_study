package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import requests.GetInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeNameByUserTest extends BaseTest {

    @Test
    public void UserCanChangeNameWithCorrectNameTest() {

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

        //изменение имени
        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name("Bon Jovi")
                .build();

        new ChangeNameRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK("message", "Profile updated successfully"))
                .post(changeNameRequest);

        // проверка того, что имя установилось
        GetInfoResponse getInfoResponse = new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(null).extract().as(GetInfoResponse.class);

        softly.assertThat(getInfoResponse.getName()).isEqualTo("Bon Jovi"); // проверка что есть пометка об успешности запроса
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ann 123", "Sara  Parker", "Mike!Shein", "David"})
    public void UserCanChangeNameWithIncorrectNameTest(String input) {
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

        //изменение имени
        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(input)
                .build();

        new ChangeNameRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Name must contain two words with letters only"))
                .post(changeNameRequest);

        // проверка того, что имя установилось
        GetInfoResponse getInfoResponse = new GetInfoRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(null).extract().as(GetInfoResponse.class);

        softly.assertThat(getInfoResponse.getName()).isEqualTo(null); // проверка что есть пометка об успешности запроса
    }
}
