package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetInfoResponse extends BaseModel{
    private long id;
    private String username;
    private String password;
    private String name;
    private UserRole role;
    private float balance;
    private List<DepositAccountResponse> accounts;
}
