package psam.portfolio.sunder.english.web.user.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserStatus {

    TRIAL,
    PENDING,
    ACTIVE,
    DORMANT,
    FORBIDDEN,
    WITHDRAWN;

    @JsonCreator
    public UserStatus of(String str) {
        return UserStatus.valueOf(str);
    }
}