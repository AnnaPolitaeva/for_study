package iteration2.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositAccount;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositByUserTest extends BaseUiTest {

    @Test
    public void userCanDepositAccountWithCorrectAmount() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getDepositMoneyButton().click();

        new DepositAccount().open().depositMoney("200", accountInfo.getId()).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_DEPOSIT_200_TO_ACCOUNT.getMessage());

        CreateAccountResponse createdAccount = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(createdAccount).isNotNull();
        assertEquals(200.00F, createdAccount.getBalance());
    }

    @Test
    public void userCanDepositAccountWithIncorrectAmount() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getDepositMoneyButton().click();

        new DepositAccount().open().depositMoney("0", accountInfo.getId()).checkAlertMessageAdnAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT.getMessage());

        CreateAccountResponse createdAccount = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(createdAccount).isNotNull();
        assertEquals(0.00F, createdAccount.getBalance());
    }
}
