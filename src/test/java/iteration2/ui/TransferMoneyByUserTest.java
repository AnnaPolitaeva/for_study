package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.DepositAccountResponse;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.LoginPage;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyByUserTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanTransferMoneyByHisAccountWithCorrectAmountTest() {
        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(SessionStorage.getUser());
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, SessionStorage.getUser(), RandomData.getBigAmount());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();

        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        float amount = RandomData.getSmallAmount();
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), String.format(Locale.US,"%.2f", amount), true).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_TRANSFERRED_TO_ACCOUNT.getFormatMessage(String.format(Locale.US,"%.2f", amount)) + secondAccountInfo.getAccountNumber() + "!");

        CreateAccountResponse accountRecipient = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(amount, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance() - amount, accountSender.getBalance());
    }

    @Test
    @UserSession(2)
    public void userCanTransferMoneyByAnotherUserAccountWithCorrectAmountTest() {
        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());
        CreateAccountResponse anotherUserAccountInfo = UserSteps.createAccount(SessionStorage.getUser(2));
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, SessionStorage.getUser(), RandomData.getBigAmount());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();

        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        float amount = RandomData.getSmallAmount();
        new TransferPage().open().makeTransfer(accountInfo.getId(), anotherUserAccountInfo.getAccountNumber(), String.format(Locale.US,"%.2f", amount), true).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_TRANSFERRED_TO_ACCOUNT.getFormatMessage(String.format(Locale.US,"%.2f", amount)) + anotherUserAccountInfo.getAccountNumber() + "!");

        CreateAccountResponse accountRecipient = SessionStorage.getSteps(2).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(anotherUserAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(amount, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance() - amount, accountSender.getBalance());
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyByHisAccountWithIncorrectAmountTest() {

        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(SessionStorage.getUser());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), RandomData.getAmountString(), true).checkAlertMessageAdnAccept(BankAlert.INVALID_TRANSFER.getMessage());

        CreateAccountResponse accountRecipient = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(accountInfo.getBalance(), accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(secondAccountInfo.getBalance(), accountSender.getBalance());
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyByHisAccountWithVeryMuchAmountTest() {
        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(SessionStorage.getUser());
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, SessionStorage.getUser(), RandomData.getBiggestAmount());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), RandomData.getBigAmountString(), true).checkAlertMessageAdnAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        CreateAccountResponse accountRecipient = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(secondAccountInfo.getBalance(), accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance(), accountSender.getBalance());
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyByHisAccountWithoutConfirmTest() {
        CreateAccountResponse accountInfo = UserSteps.createAccount(SessionStorage.getUser());
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(SessionStorage.getUser());
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, SessionStorage.getUser(), RandomData.getBigAmount());

        new LoginPage().open().login(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), RandomData.getAmountString(), false).checkAlertMessageAdnAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse accountRecipient = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = SessionStorage.getSteps().getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(secondAccountInfo.getBalance(), accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance(), accountSender.getBalance());
    }
}
