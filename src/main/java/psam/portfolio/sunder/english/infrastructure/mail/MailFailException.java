package psam.portfolio.sunder.english.infrastructure.mail;

import psam.portfolio.sunder.english.global.api.v1.ApiException;
import psam.portfolio.sunder.english.global.api.v1.ApiResponse;
import psam.portfolio.sunder.english.global.api.v1.ApiStatus;

public class MailFailException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.error(ApiStatus.INTERNAL_SERVER_ERROR, MailUtils.class, "메일을 발송하는데 실패했습니다. 메일 주소를 확인해주세요.");
    }
}
