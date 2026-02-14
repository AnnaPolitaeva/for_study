package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createAccountButton = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement usernameButton = $(Selectors.byText("Noname"));
    private SelenideElement depositMoneyButton = $(Selectors.byText("💰 Deposit Money"));
    private SelenideElement makeTransferButton = $(Selectors.byText("🔄 Make a Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount(){
        createAccountButton.click();
        return this;
    }

    public UserDashboard goToChangeName(){
        usernameButton.click();
        return this;
    }


}
