package psam.portfolio.sunder.english.web.academy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import psam.portfolio.sunder.english.infrastructure.mail.MailFailException;
import psam.portfolio.sunder.english.infrastructure.mail.MailUtils;
import psam.portfolio.sunder.english.infrastructure.password.PasswordUtils;
import psam.portfolio.sunder.english.web.academy.enumeration.AcademyStatus;
import psam.portfolio.sunder.english.web.academy.exception.DuplicateAcademyException;
import psam.portfolio.sunder.english.web.teacher.exception.RoleDirectorRequiredException;
import psam.portfolio.sunder.english.web.academy.model.entity.Academy;
import psam.portfolio.sunder.english.web.teacher.model.entity.Teacher;
import psam.portfolio.sunder.english.web.academy.model.request.AcademyDirectorPOST.AcademyPOST;
import psam.portfolio.sunder.english.web.academy.model.request.AcademyDirectorPOST.DirectorPOST;
import psam.portfolio.sunder.english.web.academy.model.request.AcademyPATCH;
import psam.portfolio.sunder.english.web.academy.repository.AcademyCommandRepository;
import psam.portfolio.sunder.english.web.academy.repository.AcademyQueryRepository;
import psam.portfolio.sunder.english.web.teacher.repository.TeacherCommandRepository;
import psam.portfolio.sunder.english.web.teacher.repository.TeacherQueryRepository;
import psam.portfolio.sunder.english.web.user.enumeration.RoleName;
import psam.portfolio.sunder.english.web.user.exception.DuplicateUserException;
import psam.portfolio.sunder.english.web.user.model.entity.UserRole;
import psam.portfolio.sunder.english.web.user.repository.UserQueryRepository;
import psam.portfolio.sunder.english.web.user.repository.UserRoleCommandRepository;

import java.util.Locale;
import java.util.UUID;

import static psam.portfolio.sunder.english.web.academy.model.entity.QAcademy.*;
import static psam.portfolio.sunder.english.web.user.enumeration.RoleName.ROLE_DIRECTOR;
import static psam.portfolio.sunder.english.web.user.enumeration.UserStatus.PENDING;
import static psam.portfolio.sunder.english.web.user.model.entity.QUser.user;

@RequiredArgsConstructor
@Transactional
@Service
public class AcademyCommandService {

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final MailUtils mailUtils;
    private final PasswordUtils passwordUtils;

    private final AcademyCommandRepository academyCommandRepository;
    private final AcademyQueryRepository academyQueryRepository;
    private final TeacherCommandRepository teacherCommandRepository;
    private final TeacherQueryRepository teacherQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserRoleCommandRepository userRoleCommandRepository;

    /**
     * 학원을 등록하는 서비스
     *
     * @param academyPOST  학원 등록 정보
     * @param directorPOST 학원장 등록 정보
     * @return 학원장의 uuid
     */
    public UUID registerDirectorWithAcademy(
            AcademyPOST academyPOST,
            DirectorPOST directorPOST
    ) {
        // academy name, phone, email 에서 중복 체크
        academyQueryRepository.findOne(
                academy.name.eq(academyPOST.getName()) // NotNull
                        .or(academyPOST.getPhone() != null ? academy.phone.eq(academyPOST.getPhone()) : null) // nullable
                        .or(academyPOST.getEmail() != null ? academy.email.eq(academyPOST.getEmail()) : null), // nullable
                academy.status.ne(AcademyStatus.PENDING)
        ).ifPresent(academy -> {
            throw new DuplicateAcademyException();
        });

        // user loginId, email, phone 에서 중복 체크. userStatusNotIn(PENDING), userEmailVerifiedEq(true)
        userQueryRepository.findOne(
                user.loginId.eq(directorPOST.getLoginId()) // NotNull
                        .or(user.email.eq(directorPOST.getEmail())) // NutNull
                        .or(directorPOST.getPhone() != null ? user.phone.eq(directorPOST.getPhone()) : null), // nullable
                user.status.ne(PENDING),
                user.emailVerified.eq(true)
        ).ifPresent(user -> {
            throw new DuplicateUserException();
        });

        // academy 생성
        Academy saveAcademy = academyCommandRepository.save(academyPOST.toEntity());

        // passwordUtils 로 loginPw 암호화
        String encodeLoginPw = passwordUtils.encode(directorPOST.getLoginPw());

        // teacher 생성
        Teacher saveDirector = teacherCommandRepository.save(directorPOST.toEntity(saveAcademy, encodeLoginPw));

        // 원장은 원장, 선생, 학생 권한 모두 취득
        UserRole userRole = UserRole.builder()
                .user(saveDirector)
                .roleName(ROLE_DIRECTOR)
                .build();
        userRoleCommandRepository.save(userRole);

        // mailUtils 로 verification mail 발송
        boolean mailResult = mailUtils.sendMail(
                saveDirector.getEmail(),
                messageSource.getMessage("mail.verification.academy.subject", null, Locale.getDefault()),
                setVerificationMailText(saveAcademy)
        );
        if (!mailResult) {
            throw new MailFailException();
        }

        return saveDirector.getUuid();
    }

    private static UserRole buildUserRole(Teacher saveDirector, RoleName roleName) {
        return UserRole.builder()
                .user(saveDirector)
                .roleName(roleName)
                .build();
    }

    // UUID from Academy
    private String setVerificationMailText(Academy academy) {
        String url = messageSource.getMessage("mail.verification.academy.url", new Object[]{academy.getUuid()}, Locale.getDefault());

        Context context = new Context();
        context.setVariable("url", url);
        return templateEngine.process("mail-verification", context);
    }

    /**
     * 학원 인증 서비스
     *
     * @param academyId 학원 아이디
     * @return 인증 성공 여부
     */
    public boolean verify(UUID academyId) {
        Academy getAcademy = academyQueryRepository.getById(academyId);

        // 학원 인증은 최초 한 번만 가능
        if (getAcademy.isVerified()) {
            return false;
        }
        getAcademy.verify();

        // 인증 시점에는 모든 선생(=학원장)의 상태를 인증함으로 변경한다.
        getAcademy.getTeachers().forEach(teacher -> {
            teacher.startTrial();
            teacher.verifyEmail(true);
        });

        return true;
    }

    /**
     * 학원 정보 수정 서비스
     *
     * @param directorId 학원장 아이디
     * @param academyPATCH 학원의 수정할 정보
     * @return 수정을 완료한 학원 아이디
     */
    public UUID updateInfo(UUID directorId, AcademyPATCH academyPATCH) {
        Teacher getDirector = teacherQueryRepository.getById(directorId);
        Academy getAcademy = getDirector.getAcademy();

        // 중복 체크. name, phone, email 중 하나라도 중복되면 예외 발생, 단, 자기 학원은 제외하며 PENDING 상태도 제외.
        academyQueryRepository.findOne(
                academy.name.eq(academyPATCH.getName())
                        .or(academyPATCH.getPhone() != null ? academy.phone.eq(academyPATCH.getPhone()) : null)
                        .or(academyPATCH.getEmail() != null ? academy.email.eq(academyPATCH.getEmail()) : null),
                academy.status.ne(AcademyStatus.PENDING),
                academy.uuid.ne(getAcademy.getUuid())
        ).ifPresent(academy -> {
            throw new DuplicateAcademyException();
        });

        // 이메일 인증은 본인인증을 위한 것.
        // 학원의 이메일은 변경해도 조치를 취하지 않는다.
        getAcademy.setName(academyPATCH.getName());
        getAcademy.setAddress(academyPATCH.getAddress());
        getAcademy.setPhone(academyPATCH.getPhone());
        getAcademy.setEmail(academyPATCH.getEmail());
        getAcademy.setOpenToPublic(academyPATCH.getOpenToPublic());

        return getAcademy.getUuid();
    }

    /**
     * 학원 폐쇄 서비스. 학원장만 가능. 폐쇄 후 7일 후에 DB 에서 완전히 삭제된다.
     * @param directorId 학원장 아이디
     * @return 폐쇄 요청한 학원 아이디
     */
    public UUID withdraw(UUID directorId) {
        Teacher director = teacherQueryRepository.getById(directorId);

        // @Secured 에서 학원장인지를 이미 검증하지만, 학원 폐쇄는 신중한 조치가 필요하므로 한 번 더 검증한다.
        if (!director.isDirector()) {
            throw new RoleDirectorRequiredException();
        }
        Academy academy = director.getAcademy();
        academy.setStatus(AcademyStatus.WITHDRAWN);
        return academy.getUuid();
    }

    // TODO scheduler 로 폐쇄 요청한지 7일이 지난 학원 삭제

    /**
     * 학원 폐쇄 취소 서비스. 학원장만 가능. 폐쇄 신청 후 7일 이내에만 가능하다.
     * @param directorId 학원장 아이디
     * @return 폐쇄 취소한 학원 아이디
     */
    public UUID revokeWithdrawal(UUID directorId) {
        Teacher director = teacherQueryRepository.getById(directorId);

        // @Secured 에서 학원장인지를 이미 검증하지만, 학원 폐쇄는 신중한 조치가 필요하므로 한 번 더 검증한다.
        if (!director.isDirector()) {
            throw new RoleDirectorRequiredException();
        }
        Academy academy = director.getAcademy();
        academy.setStatus(AcademyStatus.VERIFIED);
        return academy.getUuid();
    }
}