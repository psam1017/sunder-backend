package psam.portfolio.sunder.english.others.testconfig;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import psam.portfolio.sunder.english.global.aspect.trace.Trace;
import psam.portfolio.sunder.english.others.testbean.container.InfoContainer;
import psam.portfolio.sunder.english.others.testbean.container.StandaloneInfoContainer;
import psam.portfolio.sunder.english.others.testbean.jpa.PersistenceContextManager;

@Slf4j
@TestConfiguration
public class TestConfig {

    @Bean
    @Trace(signature = false)
    public InfoContainer uniqueInfoContainer() {
        return StandaloneInfoContainer.builder()
                .numberOfCollection(30)
                .loginIdLen(8)
                .emailLen(8)
                .emailDomain("sunder.edu")
                .academyNameMinLen(4)
                .academyNameMaxLen(8)
                .attendanceIdLen(8)
                .build();
    }

    @Bean
    public PersistenceContextManager persistenceContextManager(EntityManager em) {
        return new PersistenceContextManager(em);
    }
}
