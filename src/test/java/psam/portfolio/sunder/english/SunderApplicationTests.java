package psam.portfolio.sunder.english;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import psam.portfolio.sunder.english.docs.RestDocsConfig;
import psam.portfolio.sunder.english.infrastructure.mail.MailUtils;
import psam.portfolio.sunder.english.infrastructure.password.PasswordUtils;
import psam.portfolio.sunder.english.testbean.TestConfig;
import psam.portfolio.sunder.english.testbean.UniqueInfoContainer;
import psam.portfolio.sunder.english.domain.academy.enumeration.AcademyStatus;
import psam.portfolio.sunder.english.domain.academy.model.entity.Academy;
import psam.portfolio.sunder.english.domain.student.model.entity.Student;
import psam.portfolio.sunder.english.domain.student.repository.StudentCommandRepository;
import psam.portfolio.sunder.english.domain.teacher.model.entity.Teacher;
import psam.portfolio.sunder.english.domain.academy.repository.AcademyCommandRepository;
import psam.portfolio.sunder.english.domain.teacher.repository.TeacherCommandRepository;
import psam.portfolio.sunder.english.domain.user.enumeration.RoleName;
import psam.portfolio.sunder.english.domain.user.enumeration.UserStatus;
import psam.portfolio.sunder.english.domain.user.model.entity.User;
import psam.portfolio.sunder.english.domain.user.model.entity.UserRole;
import psam.portfolio.sunder.english.domain.user.repository.UserRoleCommandRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@ExtendWith(RestDocumentationExtension.class)
@Import({TestConfig.class, RestDocsConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class SunderApplicationTests {

	@Test
	void contextLoads() {

	}

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper om;

	@Autowired
	protected EntityManager em;

	@Autowired
	protected UniqueInfoContainer uic;

	@MockBean
	protected MailUtils mailUtils;

	@Autowired
	private StudentCommandRepository studentCommandRepository;

	@Autowired
	private TeacherCommandRepository teacherCommandRepository;

	@Autowired
	private AcademyCommandRepository academyCommandRepository;

	@Autowired
	private UserRoleCommandRepository userRoleCommandRepository;

	@Autowired
	private PasswordUtils passwordUtils;

	protected String createJson(Object body) {
		try {
			return om.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T refreshAnd(Supplier<T> action) {
		em.flush();
		em.clear();
		System.out.println("\n#============================== Flush and Clear. Action Start. ==============================\n");
		T result = action.get();
		em.flush();
		em.clear();
		System.out.println("\n#============================== Flush and Clear. Action Finished. ==============================\n");
		return result;
	}

	protected Academy registerAcademy(AcademyStatus status) {
		Academy academy = Academy.builder()
				.name(uic.getUniqueAcademyName())
				.address(uic.getAnyAddress())
				.phone(uic.getUniquePhoneNumber())
				.email(uic.getUniqueEmail())
				.openToPublic(true)
				.status(status)
				.build();
		return academyCommandRepository.save(academy);
	}

	protected Academy registerAcademy(boolean openToPublic, AcademyStatus status) {
		Academy academy = Academy.builder()
				.name(uic.getUniqueAcademyName())
				.address(uic.getAnyAddress())
				.phone(uic.getUniquePhoneNumber())
				.email(uic.getUniqueEmail())
				.openToPublic(openToPublic)
				.status(status)
				.build();
		return academyCommandRepository.save(academy);
	}

	protected Teacher registerTeacher(UserStatus status, Academy academy) {
		String uniqueId = uic.getUniqueLoginId();
		Teacher teacher = Teacher.builder()
				.loginId(uniqueId)
				.loginPw(passwordUtils.encode("qwe123!@#"))
				.name("사용자" + uniqueId.substring(0, 3))
				.email(uic.getUniqueEmail())
				.emailVerified(true)
				.phone(uic.getUniquePhoneNumber())
				.address(uic.getAnyAddress())
				.status(status)
				.academy(academy)
				.build();
		return teacherCommandRepository.save(teacher);
	}

	protected Teacher registerTeacher(String name, UserStatus status, Academy academy) {
		String uniqueId = uic.getUniqueLoginId();
		Teacher teacher = Teacher.builder()
				.loginId(uniqueId)
				.loginPw(passwordUtils.encode("qwe123!@#"))
				.name(name)
				.email(uic.getUniqueEmail())
				.emailVerified(true)
				.phone(uic.getUniquePhoneNumber())
				.address(uic.getAnyAddress())
				.status(status)
				.academy(academy)
				.build();
		return teacherCommandRepository.save(teacher);
	}

	protected Student registerStudent(UserStatus status, Academy academy) {
		String uniqueId = uic.getUniqueLoginId();
		Student student = Student.builder()
				.loginId(uniqueId)
				.loginPw(passwordUtils.encode("qwe123!@#"))
				.name("사용자" + uniqueId.substring(0, 3))
				.email(uic.getUniqueEmail())
				.emailVerified(true)
				.phone(uic.getUniquePhoneNumber())
				.attendanceId(uic.getUniqueAttendanceId())
				.note("note about student")
				.address(uic.getAnyAddress())
				.school(uic.getAnySchool())
				.parent(uic.getAnyParent())
				.status(status)
				.academy(academy)
				.build();
		return studentCommandRepository.save(student);
	}

	protected List<UserRole> createRole(User user, RoleName... roleNames) {
		List<UserRole> userRoles = new ArrayList<>();
		for (RoleName rn : roleNames) {
			UserRole userRole = UserRole.builder().user(user).roleName(rn).build();
			userRoles.add(userRole);
		}
		return userRoleCommandRepository.saveAll(userRoles);
	}
}
