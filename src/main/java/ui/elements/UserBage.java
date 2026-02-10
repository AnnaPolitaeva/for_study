package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class UserBage extends BaseElement {
    private String usename;
    private String role;

    public UserBage(SelenideElement element) {
        super(element);
        usename = element.getText().split("\n")[0];
        role = element.getText().split("\n")[1];
    }
}
