package iteration2.ui;

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
    public void userCanChangeNameWithCorrectNameTest() {

        CreateUserRequest user = AdminSteps.createUser().request();
        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName("Bon Jovi").checkAlertMessageAdnAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Bon Jovi");

        // ШАГ 6: проверка, что имя было изменено на API
        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNotNull();
        assertEquals("Bon Jovi", userInfo.getName());
    }

    @Test
    public void userCanChangeNameWithIncorrectNameTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName("David").checkAlertMessageAdnAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Noname");

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNull();
    }

    @Test
    public void userCanChangeNameWithEmptyNameTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
        authAsUser(user);

        new UserDashboard().open().goToChangeName().getPage(EditProfile.class).getEditProfileText().shouldBe(Condition.visible);

        new EditProfile().changeName(null).checkAlertMessageAdnAccept(BankAlert.PLEASE_ENTER_A_VALID_NAME.getMessage());

        Selenide.refresh();

        new EditProfile().checkUsername(user.getUsername(), "Noname");

        GetInfoResponse userInfo = new UserSteps(user.getUsername(), user.getPassword()).getUserInfo();

        assertThat(userInfo.getName()).isNull();
    }
}
