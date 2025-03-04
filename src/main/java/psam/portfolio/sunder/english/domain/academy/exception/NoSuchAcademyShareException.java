package psam.portfolio.sunder.english.domain.academy.exception;

import psam.portfolio.sunder.english.domain.academy.model.entity.AcademyShare;
import psam.portfolio.sunder.english.global.api.v1.ApiException;
import psam.portfolio.sunder.english.global.api.v1.ApiResponse;
import psam.portfolio.sunder.english.global.api.v1.ApiStatus;

public class NoSuchAcademyShareException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.error(ApiStatus.NO_SUCH_ELEMENT, AcademyShare.class, "존재하지 않는 공유대상입니다.");
    }
}
