package psam.portfolio.sunder.english.global.command;

import psam.portfolio.sunder.english.global.api.v1.ApiException;
import psam.portfolio.sunder.english.global.api.v1.ApiResponse;
import psam.portfolio.sunder.english.global.api.v1.ApiStatus;

public class CommandException extends ApiException {
    
    // 요청한 CRUDCommand 를 처리할 수 없을 때 생성
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.error(ApiStatus.ILLEGAL_DATA, CRUDCommand.class, "커맨드 요청을 처리할 수 없습니다.");
    }
}
