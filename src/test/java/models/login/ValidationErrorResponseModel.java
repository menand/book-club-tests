package models.login;

import java.util.List;

public record ValidationErrorResponseModel(List<String> username, List<String> password) {
    public boolean hasUsernameError() {
        return username != null && !username.isEmpty();
    }

    public boolean hasPasswordError() {
        return password != null && !password.isEmpty();
    }

    public String getUsernameError() {
        return hasUsernameError() ? username.getFirst() : null;
    }

    public String getPasswordError() {
        return hasPasswordError() ? password.getFirst() : null;
    }
}
