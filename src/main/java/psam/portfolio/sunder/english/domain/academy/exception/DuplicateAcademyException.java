package psam.portfolio.sunder.english.domain.academy.exception;

import psam.portfolio.sunder.english.global.api.v1.ApiException;
import psam.portfolio.sunder.english.global.api.v1.ApiResponse;
import psam.portfolio.sunder.english.global.api.v1.ApiStatus;
import psam.portfolio.sunder.english.domain.academy.model.entity.Academy;

public class DuplicateAcademyException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.error(ApiStatus.DUPLICATE_KEY, Academy.class, "중복된 학원 정보가 있습니다.");
    }
}
