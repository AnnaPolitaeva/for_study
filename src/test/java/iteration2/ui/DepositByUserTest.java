package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositAccount;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositByUserTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanDepositAccountWithCorrectAmount() {
        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getDepositMoneyButton().click();

        float amount = RandomData.getSmallAmount();
        new DepositAccount().open().depositMoney(String.format(Locale.US,"%.2f", amount), accountInfo.getId()).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_DEPOSIT_TO_ACCOUNT.getFormatMessage(String.format(Locale.US,"%.2f", amount)));

        CreateAccountResponse createdAccount = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(createdAccount).isNotNull();
        assertEquals(amount, createdAccount.getBalance());
    }

    @Test
    @UserSession
    public void userCanDepositAccountWithIncorrectAmount() {
        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getDepositMoneyButton().click();

        new DepositAccount().open().depositMoney("0", accountInfo.getId()).checkAlertMessageAdnAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT.getMessage());

        CreateAccountResponse createdAccount = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(createdAccount).isNotNull();
        assertEquals(0.00F, createdAccount.getBalance());
    }
}
