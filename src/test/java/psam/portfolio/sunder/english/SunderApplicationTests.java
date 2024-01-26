package psam.portfolio.sunder.english;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import psam.portfolio.sunder.english.global.jpa.embeddable.Address;
import psam.portfolio.sunder.english.testbean.TestConfig;
import psam.portfolio.sunder.english.testbean.UniqueInfoContainer;
import psam.portfolio.sunder.english.web.teacher.enumeration.AcademyStatus;
import psam.portfolio.sunder.english.web.teacher.model.Academy;
import psam.portfolio.sunder.english.web.teacher.model.Teacher;
import psam.portfolio.sunder.english.web.teacher.repository.TeacherCommandRepository;
import psam.portfolio.sunder.english.web.user.enumeration.UserStatus;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@Import(TestConfig.class)
public class SunderApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private UniqueInfoContainer uic;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper om;

	@Autowired
	protected EntityManager em;

	protected String createJson(Object body) {
		try {
			return om.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Autowired
	private TeacherCommandRepository teacherCommandRepository;

	protected Academy registerAcademy(AcademyStatus status) {
		Academy academy = Academy.builder()
				.name(uic.getUniqueAcademyName())
				.address(uic.getAnyAddress())
				.phone(null)
				.email(null)
				.openToPublic(true)
				.status(status)
				.build();
		return em.merge(academy);
	}

	protected Teacher registerTeacher(UserStatus status, Academy academy) {
		String uniqueId = uic.getUniqueId();
		Teacher teacher = Teacher.builder()
				.loginId(uniqueId)
				.loginPw("qwe123!@#")
				.name("사용자" + uniqueId.substring(0, 3))
				.email(uic.getUniqueEmail())
				.emailVerified(true)
				.phone(uic.getUniquePhoneNumber())
				.status(status)
				.academy(academy)
				.build();
		return teacherCommandRepository.save(teacher);
	}
}
