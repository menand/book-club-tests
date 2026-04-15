package models.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String remoteAddr;
}
