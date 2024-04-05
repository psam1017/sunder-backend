package psam.portfolio.sunder.english.domain.student.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import psam.portfolio.sunder.english.domain.student.model.entity.Student;
import psam.portfolio.sunder.english.domain.user.enumeration.RoleName;
import psam.portfolio.sunder.english.global.jpa.response.AddressResponse;
import psam.portfolio.sunder.english.global.jsonformat.KoreanDateTime;
import psam.portfolio.sunder.english.domain.user.enumeration.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Builder
@Getter
@AllArgsConstructor(access = PRIVATE)
public class StudentPublicResponse {

    private UUID id;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    private AddressResponse address;
    private UserStatus status;
    private String attendanceId;
    private String schoolName;
    private Integer schoolGrade;
    private String parentName;
    private String parentPhone;
    @KoreanDateTime
    private LocalDateTime createdDateTime;
    @KoreanDateTime
    private LocalDateTime modifiedDateTime;
    private UUID createdBy;
    private UUID modifiedBy;

   public static StudentPublicResponse from(Student student) {
        return StudentPublicResponse.builder()
                .id(student.getUuid())
                .loginId(student.getLoginId())
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(AddressResponse.from(student.getAddress()))
                .status(student.getStatus())
                .attendanceId(student.getAttendanceId())
                .schoolName(student.getSchool().getSchoolName())
                .schoolGrade(student.getSchool().getSchoolGrade())
                .parentName(student.getParent().getParentName())
                .parentPhone(student.getParent().getParentPhone())
                .createdDateTime(student.getCreatedDateTime())
                .modifiedDateTime(student.getModifiedDateTime())
                .createdBy(student.getCreatedBy())
                .modifiedBy(student.getModifiedBy())
                .build();
    }
}
