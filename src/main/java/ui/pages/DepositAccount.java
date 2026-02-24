package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositAccount extends BasePage<DepositAccount>{
    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositAccount depositMoney(String amount, long accountId){
        accountSelector.selectOptionByValue(String.valueOf(accountId));
        enterAmountInput.sendKeys(amount);
        depositButton.click();
        return this;
    }
}
