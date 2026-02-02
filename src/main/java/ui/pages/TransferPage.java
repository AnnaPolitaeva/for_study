package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class TransferPage extends BasePage<TransferPage>{
    private SelenideElement newTransferText = $(Selectors.byText("🆕 New Transfer"));
    private SelenideElement enterRecipientAccountNumberInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement confirmCheckbox = $(Selectors.byAttribute("type", "checkbox"));
    private SelenideElement sendTranferButton = $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/";
    }

    public TransferPage makeTransfer(long accountId, String recipientAccountNumber, String amount, boolean confirm){
        accountSelector.selectOptionByValue(String.valueOf(accountId));
        enterRecipientAccountNumberInput.sendKeys(recipientAccountNumber);
        enterAmountInput.sendKeys(amount);
        if (!confirm){
            confirmCheckbox.click();
        }
        sendTranferButton.click();
        return this;
    }
}
