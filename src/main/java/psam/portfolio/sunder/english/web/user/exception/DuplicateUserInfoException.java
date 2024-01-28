package psam.portfolio.sunder.english.web.user.exception;

import psam.portfolio.sunder.english.global.api.ApiException;
import psam.portfolio.sunder.english.global.api.ApiResponse;
import psam.portfolio.sunder.english.global.api.ApiStatus;
import psam.portfolio.sunder.english.web.user.model.User;

public class DuplicateUserInfoException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.error(ApiStatus.DUPLICATE_KEY, User.class, "중복된 사용자 정보가 있습니다.");
    }
}
