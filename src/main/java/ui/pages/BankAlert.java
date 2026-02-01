package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATE_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATE_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only"),
    PLEASE_ENTER_A_VALID_NAME("❌ Please enter a valid name."),
    PLEASE_ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),
    SUCCESSFULLY_DEPOSIT_200_TO_ACCOUNT("✅ Successfully deposited $200 to account "),
    SUCCESSFULLY_TRANSFERRED_10_TO_ACCOUNT("✅ Successfully transferred $10 to account "),
    INVALID_TRANSFER("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    PLEASE_FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm.");

    private final String message;

    BankAlert(String message){
        this.message = message;
    }
}
