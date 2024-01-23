package psam.portfolio.sunder.english.web.teacher.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import psam.portfolio.sunder.english.global.audit.BaseEntity;
import psam.portfolio.sunder.english.web.lesson.entity.Lesson;
import psam.portfolio.sunder.english.web.student.entity.Student;
import psam.portfolio.sunder.english.web.teacher.enumeration.AcademyStatus;

import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "academies")
@Entity
public class Academy extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    private String name;
    private String address;
    private String phone;
    private String email;
    private boolean openToPublic;

    @Enumerated(EnumType.STRING)
    private AcademyStatus status;

    @OneToMany(mappedBy = "academy")
    private Set<Teacher> teachers;

    @OneToMany(mappedBy = "academy")
    private Set<Student> students;

    @OneToMany(mappedBy = "academy")
    private Set<Lesson> lessons;

    @Builder
    public Academy(String name, String address, String phone, String email, boolean openToPublic, AcademyStatus status) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.openToPublic = openToPublic;
        this.status = status;
    }
}
