package psam.portfolio.sunder.english.web.teacher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import psam.portfolio.sunder.english.global.pagination.PageInfo;
import psam.portfolio.sunder.english.web.teacher.enumeration.AcademyStatus;
import psam.portfolio.sunder.english.web.teacher.exception.OneParamToCheckAcademyDuplException;
import psam.portfolio.sunder.english.web.teacher.model.entity.Academy;
import psam.portfolio.sunder.english.web.teacher.model.entity.Teacher;
import psam.portfolio.sunder.english.web.teacher.model.request.AcademyPublicSearchCond;
import psam.portfolio.sunder.english.web.teacher.model.response.AcademyFullResponse;
import psam.portfolio.sunder.english.web.teacher.model.response.TeacherFullResponse;
import psam.portfolio.sunder.english.web.teacher.repository.AcademyQueryRepository;
import psam.portfolio.sunder.english.web.teacher.repository.TeacherQueryRepository;

import java.util.*;

import static psam.portfolio.sunder.english.web.teacher.model.entity.QAcademy.academy;
import static psam.portfolio.sunder.english.web.user.enumeration.RoleName.ROLE_DIRECTOR;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AcademyQueryService {

    private final AcademyQueryRepository academyQueryRepository;
    private final TeacherQueryRepository teacherQueryRepository;

    /**
     * 학원 등록 시 중복 체크 서비스
     *
     * @param name  학원 이름
     * @param phone 학원 전화번호
     * @param email 학원 이메일
     * @return 중복 여부
     */
    public boolean checkDuplication(String name, String phone, String email) {
        boolean hasName = StringUtils.hasText(name);
        boolean hasPhone = StringUtils.hasText(phone);
        boolean hasEmail = StringUtils.hasText(email);

        if (!hasOnlyOne(hasName, hasPhone, hasEmail)) {
            throw new OneParamToCheckAcademyDuplException();
        }

        Optional<Academy> optAcademy = Optional.empty();
        if (hasName) {
            optAcademy = academyQueryRepository.findOne(
                    academy.name.eq(name),
                    academy.status.ne(AcademyStatus.PENDING)
            );
        } else if (hasPhone) {
            optAcademy = academyQueryRepository.findOne(
                    academy.phone.eq(phone),
                    academy.status.ne(AcademyStatus.PENDING)
            );
        } else if (hasEmail) {
            optAcademy = academyQueryRepository.findOne(
                    academy.email.eq(email),
                    academy.status.ne(AcademyStatus.PENDING)
            );
        }
        return optAcademy.isEmpty();
    }

    private static boolean hasOnlyOne(boolean a, boolean b, boolean c) {
        return a ^ b ^ c && !(a && b && c);
    }


    /**
     * 학원 상세 정보 조회 서비스
     * 단, 해당 학원에 소속된 학원장 및 선생만 조회할 수 있다.
     *
     * @param teacherId 선생 아이디
     * @param select    같이 조회할 정보 = {teacher}
     * @return 학원 상세 정보 + (선생 목록)
     */
    public Map<String, Object> getDetail(UUID teacherId, String select) {

        // 권한 검증 : 학원장, 선생만 자기 학원을 조회 가능
        Teacher getTeacher = teacherQueryRepository.getById(teacherId);
        Academy getAcademy = getTeacher.getAcademy();

        // 응답값 구성
        Map<String, Object> response = new HashMap<>();

        // 학원 정보 조회
        AcademyFullResponse academyFullResponse = AcademyFullResponse.from(getAcademy);
        response.put("academy", academyFullResponse);

        // 선생 정보 추가 조회
        if (StringUtils.hasText(select)) {
            select = select.length() > 20 ? select.substring(0, 20) : select;

            // 정렬 순서 : 원장 > 상태 > 이름
            if (select.contains("teacher")) {
                List<TeacherFullResponse> teacherFullResponses = getAcademy.getTeachers().stream()
                        .map(TeacherFullResponse::from)
                        .sorted(Comparator.comparing((TeacherFullResponse t) ->
                                t.getRoles().stream().anyMatch(r -> r == ROLE_DIRECTOR) ? 0 : 1
                        ).thenComparing((TeacherFullResponse t) ->
                                switch (t.getStatus()) {
                                    case ACTIVE -> 0;
                                    case TRIAL -> 1;
                                    case PENDING -> 2;
                                    case WITHDRAWN -> 3;
                                    case FORBIDDEN -> 4;
                                    case TRIAL_END -> 5;
                                }
                        ).thenComparing(TeacherFullResponse::getName))
                        .toList();
                response.put("teachers", teacherFullResponses);
            }
        }

        return response;
    }

    /**
     * 학원 목록 조회 서비스
     * 단, openToPublic 이 true 인 학원만 조회할 수 있다.
     *
     * @param cond 검색 조건
     * @return 학원 목록과 페이징 정보
     */
    public Map<String, Object> getPublicList(AcademyPublicSearchCond cond) {
        List<Academy> academies = academyQueryRepository.pageBySearchCond(cond);
        Long count = academyQueryRepository.countBySearchCond(academies, cond);
        List<AcademyFullResponse> responses = academies.stream().map(AcademyFullResponse::from).toList();
        PageInfo pageInfo = new PageInfo(cond.getPage(), cond.getSize(), count, 10);
        return Map.of(
                "academies", responses,
                "pageInfo", pageInfo
        );
    }
}
