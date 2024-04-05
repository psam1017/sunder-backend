package psam.portfolio.sunder.english.testbean.container;

import psam.portfolio.sunder.english.global.jpa.embeddable.Address;
import psam.portfolio.sunder.english.domain.student.model.embeddable.Parent;
import psam.portfolio.sunder.english.domain.student.model.embeddable.School;

public interface InfoContainer {

    String getUniqueLoginId();
    String getUniqueEmail();
    String getUniquePhoneNumber();
    String getUniqueAcademyName();
    String getUniqueAttendanceId();

    default String getRawPassword() {
        return "qwe123!@#";
    }

    default Address getAnyAddress() {
        return Address.builder()
                .street("서울특별시 선더구 선더로 1")
                .detail("선더빌딩")
                .postalCode("00000")
                .build();
    }

    default School getAnySchool() {
        return School.builder()
                .schoolName("선더초등학교")
                .schoolGrade(3)
                .build();
    }

    default Parent getAnyParent() {
        return Parent.builder()
                .parentName("홍길동")
                .parentPhone("01012345678")
                .build();
    }
}
