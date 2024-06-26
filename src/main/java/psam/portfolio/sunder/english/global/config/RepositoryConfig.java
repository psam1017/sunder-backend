package psam.portfolio.sunder.english.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import psam.portfolio.sunder.english.global.p6spy.P6SpyEventListener;
import psam.portfolio.sunder.english.global.p6spy.P6SpyFormatter;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("PatternVariableCanBeUsed")
@EnableJpaAuditing
@Configuration
public class RepositoryConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public P6SpyEventListener p6SpyCustomEventListener() {
        return new P6SpyEventListener();
    }

    @Bean
    public P6SpyFormatter p6SpyCustomFormatter() {
        return new P6SpyFormatter();
    }

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof UserDetails userDetails) {

                return Optional.of(UUID.fromString(userDetails.getUsername()));
            }
            return Optional.empty();
        };
    }
}
