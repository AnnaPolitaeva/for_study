package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    private long id;
    private float amount;
    private TransactionType type;
    private String timestamp;
    private long relatedAccountId;
}
