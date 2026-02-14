package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositAccountResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
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
    public void userCanTransferMoneyByHisAccountWithCorrectAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, user, RandomData.getBigAmount());

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();

        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        float amount = RandomData.getSmallAmount();
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), String.format(Locale.US,"%.2f", amount), true).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_TRANSFERRED_TO_ACCOUNT.getFormatMessage(String.format(Locale.US,"%.2f", amount)) + secondAccountInfo.getAccountNumber() + "!");

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(amount, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance() - amount, accountSender.getBalance());
    }

    @Test
    public void userCanTransferMoneyByAnotherUserAccountWithCorrectAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateUserRequest anotherUser = AdminSteps.createUser().request();
//        authAsUser(anotherUser);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse anotherUserAccountInfo = UserSteps.createAccount(anotherUser);
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, user, RandomData.getBigAmount());

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();

        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        float amount = RandomData.getSmallAmount();
        new TransferPage().open().makeTransfer(accountInfo.getId(), anotherUserAccountInfo.getAccountNumber(), String.format(Locale.US,"%.2f", amount), true).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_TRANSFERRED_TO_ACCOUNT.getFormatMessage(String.format(Locale.US,"%.2f", amount)) + anotherUserAccountInfo.getAccountNumber() + "!");

        CreateAccountResponse accountRecipient = new UserSteps(anotherUser.getUsername(), anotherUser.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(anotherUserAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(amount, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance() - amount, accountSender.getBalance());
    }

    @Test
    public void userCanNotTransferMoneyByHisAccountWithIncorrectAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), RandomData.getAmountString(), true).checkAlertMessageAdnAccept(BankAlert.INVALID_TRANSFER.getMessage());

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(accountInfo.getBalance(), accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(secondAccountInfo.getBalance(), accountSender.getBalance());
    }

    @Test
    public void userCanNotTransferMoneyByHisAccountWithVeryMuchAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, user, RandomData.getBiggestAmount());

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), RandomData.getBigAmountString(), true).checkAlertMessageAdnAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(secondAccountInfo.getBalance(), accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance(), accountSender.getBalance());
    }

    @Test
    public void userCanNotTransferMoneyByHisAccountWithoutConfirmTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        DepositAccountResponse depositAccountResponse = UserSteps.depositAccount(accountInfo, user, RandomData.getBigAmount());

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), RandomData.getAmountString(), false).checkAlertMessageAdnAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(secondAccountInfo.getBalance(), accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(depositAccountResponse.getBalance(), accountSender.getBalance());
    }
}
