package psam.portfolio.sunder.english.web.teacher.contoller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import psam.portfolio.sunder.english.global.api.ApiResponse;
import psam.portfolio.sunder.english.global.resolver.argument.UserId;
import psam.portfolio.sunder.english.web.teacher.model.request.AcademyDirectorPOST;
import psam.portfolio.sunder.english.web.teacher.model.request.AcademyPATCH;
import psam.portfolio.sunder.english.web.teacher.model.request.AcademyPublicSearchCond;
import psam.portfolio.sunder.english.web.teacher.service.AcademyCommandService;
import psam.portfolio.sunder.english.web.teacher.service.AcademyQueryService;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/academy")
@RestController
public class AcademyController {

    private final AcademyCommandService academyCommandService;
    private final AcademyQueryService academyQueryService;

    /**
     * 학원 이름 중복 체크 서비스
     *
     * @param name  학원 이름
     * @param phone 학원 전화번호
     * @param email 학원 이메일
     * @return 중복 여부
     */
    @GetMapping("/check-dupl")
    public ApiResponse<Map<String, Boolean>> checkDuplication(@RequestParam(required = false) String name,
                                                              @RequestParam(required = false) String phone,
                                                              @RequestParam(required = false) String email) {
        boolean result = academyQueryService.checkDuplication(name, phone, email);
        return ApiResponse.ok(Map.of("isOk", result));
    }

    /**
     * 학원과 학원장을 등록하는 서비스
     *
     * @param post 학원과 학원장 정보
     * @return 학원장 uuid
     */
    @PostMapping("")
    public ApiResponse<Map<String, UUID>> register(@RequestBody @Valid AcademyDirectorPOST post) {
        UUID directorId = academyCommandService.registerDirectorWithAcademy(post.getAcademy(), post.getDirector());
        return ApiResponse.ok(Map.of("directorId", directorId));
    }

    /**
     * 학원 검증 및 승인 서비스
     *
     * @param academyId 학원 아이디
     * @return 학원 승인 여부
     */
    @PostMapping("/{academyId}/verify")
    public ApiResponse<Map<String, Boolean>> verify(@PathVariable String academyId) {
        boolean result = academyCommandService.verify(UUID.fromString(academyId));
        return ApiResponse.ok(Map.of("verified", result));
    }

    /**
     * 학원 상세 정보 조회 서비스. 자기 학원의 정보만 조회할 수 있다.
     *
     * @param teacherId 선생 아이디
     * @param select    같이 조회할 정보 = {teacher}
     * @return 학원 상세 정보 + (선생 목록)
     */
    @GetMapping("")
    @Secured({"ROLE_DIRECTOR", "ROLE_TEACHER"})
    public ApiResponse<Map<String, Object>> getDetail(@UserId UUID teacherId,
                                                      @RequestParam(required = false) String select) {
        Map<String, Object> responseData = academyQueryService.getDetail(teacherId, select);
        return ApiResponse.ok(responseData);
    }

    /**
     * 학원 정보 수정 서비스. 본인의 학원만 수정할 수 있다.
     *
     * @param directorId   학원장 아이디
     * @param academyPATCH 학원의 수정할 정보
     * @return 수정을 완료한 학원 아이디
     */
    @Secured("ROLE_DIRECTOR")
    @PatchMapping("")
    public ApiResponse<Map<String, UUID>> updateInfo(@UserId UUID directorId,
                                                     @RequestBody @Valid AcademyPATCH academyPATCH) {
        UUID academyId = academyCommandService.updateInfo(directorId, academyPATCH);
        return ApiResponse.ok(Map.of("academyId", academyId));
    }

    /**
     * openToPublic = true 인 공개 학원 목록 조회 서비스
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param prop 정렬 기준
     * @param dir  정렬 방향
     * @return 학원 목록
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getPublicList(@RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer size,
                                                          @RequestParam(required = false) String prop,
                                                          @RequestParam(required = false) String dir,
                                                          @RequestParam(required = false) String academyName) {
        AcademyPublicSearchCond buildCond = AcademyPublicSearchCond.builder()
                .page(page)
                .size(size)
                .prop(prop)
                .order(dir)
                .academyName(academyName)
                .build();

        Map<String, Object> response = academyQueryService.getPublicList(buildCond);
        return ApiResponse.ok(response);
    }
}
