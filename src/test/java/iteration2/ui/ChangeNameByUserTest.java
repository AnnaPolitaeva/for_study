package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.GetInfoResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfile;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeNameByUserTest extends BaseUiTest {

    @Test
    public void userCanChangeNameWithCorrectNameTest(){
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        String newName = RandomData.getName();

        new EditProfile().changeName(newName).checkAlertMessageAdnAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), newName);

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNotNull();
        assertEquals(newName, userInfo.getName());
    }

    @Test
    public void userCanChangeNameWithIncorrectNameTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName(RandomData.getIncorrectName()).checkAlertMessageAdnAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Noname");

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNull();
    }

    @Test
    public void userCanChangeNameWithEmptyNameTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName(null).checkAlertMessageAdnAccept(BankAlert.PLEASE_ENTER_A_VALID_NAME.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Noname");

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNull();
    }
}
