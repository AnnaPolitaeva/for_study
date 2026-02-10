package iteration2.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Condition;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.LoginPage;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyByUserTest extends BaseUiTest {

    @Test
    public void userCanTransferMoneyByHisAccountWithCorrectAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        UserSteps.depositAccount(accountInfo, user, 200F);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();

        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), "10", true).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_TRANSFERRED_10_TO_ACCOUNT.getMessage() + secondAccountInfo.getAccountNumber() + "!");

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(10.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(190.00F, accountSender.getBalance());
    }

    @Test
    public void userCanTransferMoneyByAnotherUserAccountWithCorrectAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateUserRequest anotherUser = AdminSteps.createUser().request();
//        authAsUser(anotherUser);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse anotherUserAccountInfo = UserSteps.createAccount(anotherUser);
        UserSteps.depositAccount(accountInfo, user, 200F);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), anotherUserAccountInfo.getAccountNumber(), "10", true).checkAlertMessageAdnAccept(BankAlert.SUCCESSFULLY_TRANSFERRED_10_TO_ACCOUNT.getMessage() + anotherUserAccountInfo.getAccountNumber() + "!");

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(anotherUserAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(anotherUser.getUsername(), anotherUser.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(10.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(190.00F, accountSender.getBalance());
    }

    @Test
    public void userCanNotTransferMoneyByHisAccountWithIncorrectAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        UserSteps.depositAccount(accountInfo, user, 5F);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), "10", true).checkAlertMessageAdnAccept(BankAlert.INVALID_TRANSFER.getMessage());

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(0.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(5.00F, accountSender.getBalance());
    }

    @Test
    public void userCanNotTransferMoneyByHisAccountWithVeryMuchAmountTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        UserSteps.depositAccount(accountInfo, user, 15000F);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), "10500", true).checkAlertMessageAdnAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(0.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(15000F, accountSender.getBalance());
    }

    @Test
    public void userCanNotTransferMoneyByHisAccountWithoutConfirmTest() {
        CreateUserRequest user = AdminSteps.createUser().request();
//        authAsUser(user);

        CreateAccountResponse accountInfo = UserSteps.createAccount(user);
        CreateAccountResponse secondAccountInfo = UserSteps.createAccount(user);
        UserSteps.depositAccount(accountInfo, user, 15000F);

        new LoginPage().open().login(user.getUsername(), user.getPassword()).getPage(UserDashboard.class).getMakeTransferButton().click();
        new TransferPage().open().getNewTransferText().shouldBe(Condition.visible);
        new TransferPage().open().makeTransfer(accountInfo.getId(), secondAccountInfo.getAccountNumber(), "10", false).checkAlertMessageAdnAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        CreateAccountResponse accountRecipient = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(secondAccountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        CreateAccountResponse accountSender = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts().stream().filter(account -> account.getAccountNumber().equals(accountInfo.getAccountNumber()))
                .findFirst().orElse(null);

        assertThat(accountRecipient).isNotNull();
        assertEquals(0.00F, accountRecipient.getBalance());

        assertThat(accountSender).isNotNull();
        assertEquals(200.00F, accountSender.getBalance());
    }
}
