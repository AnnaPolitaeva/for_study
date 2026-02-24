package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class EditProfile extends BasePage<EditProfile> {
    private SelenideElement editProfileText = $(Selectors.byText("✏️ Edit Profile"));
    private SelenideElement enterNewNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("💾 Save Changes"));
    private SelenideElement usernameText;

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile changeName(String newName) {
        if (newName != null) {
            enterNewNameInput.setValue(newName).shouldHave(Condition.value(newName));
        }
        saveChangesButton.click();
        return this;
    }

    public EditProfile checkUsername(String username, String newName) {
        usernameText = $(Selectors.byText(username)).parent().shouldHave(text(newName));
        return this;
    }
}
