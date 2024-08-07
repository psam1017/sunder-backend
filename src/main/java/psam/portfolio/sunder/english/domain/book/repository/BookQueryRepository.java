package psam.portfolio.sunder.english.domain.book.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import psam.portfolio.sunder.english.domain.book.model.enumeration.BookStatus;
import psam.portfolio.sunder.english.domain.book.exception.NoSuchBookException;
import psam.portfolio.sunder.english.domain.book.model.entity.Book;
import psam.portfolio.sunder.english.domain.book.model.request.BookPageSearchCond;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static psam.portfolio.sunder.english.domain.book.model.entity.QBook.book;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Repository
public class BookQueryRepository {

    private final JPAQueryFactory query;
    private final EntityManager em;

    public Book getById(UUID uuid) {
        Book entity = em.find(Book.class, uuid);
        if (entity == null) {
            throw new NoSuchBookException();
        }
        return entity;
    }

    public Book getOne(BooleanExpression... expressions) {
        Book entity = query
                .select(book)
                .from(book)
                .where(expressions)
                .fetchOne();
        if (entity == null) {
            throw new NoSuchBookException();
        }
        return entity;
    }

    public Optional<Book> findById(UUID uuid) {
        return Optional.ofNullable(em.find(Book.class, uuid));
    }

    public Optional<Book> findOne(BooleanExpression... expressions) {
        return Optional.ofNullable(
                query.select(book)
                        .from(book)
                        .where(expressions)
                        .fetchOne()
        );
    }

    public List<Book> findAll(BooleanExpression... expressions) {
        return query.select(book)
                .from(book)
                .where(expressions)
                .fetch();
    }

    public List<Book> findAllByPageSearchCond(UUID academyId, BookPageSearchCond cond) {
        return query.selectDistinct(book)
                .from(book)
                .where(
                        academyIdEqOrIsNullByPrivateOnly(academyId, cond.isPrivateOnly()),
                        createdDateTimeYearEq(cond),
                        searchTextContains(cond.getSplitKeyword())
                )
                .orderBy(book.createdDateTime.desc())
                .offset(cond.getOffset())
                .limit(cond.getSize())
                .fetch();
    }

    public long countByPageSearchCond(long contentSize, UUID academyId, BookPageSearchCond cond) {
        int size = cond.getSize();
        long offset = cond.getOffset();

        if (offset == 0) {
            if (size > contentSize) {
                return contentSize;
            }
            return this.countByPageSearchCondQuery(academyId, cond);
        }
        if (contentSize != 0 && size > contentSize) {
            return offset + contentSize;
        }
        return this.countByPageSearchCondQuery(academyId, cond);
    }

    private long countByPageSearchCondQuery(UUID academyId, BookPageSearchCond cond) {
        Long count = query.select(book.countDistinct())
                .from(book)
                .where(
                        academyIdEqOrIsNullByPrivateOnly(academyId, cond.isPrivateOnly()),
                        createdDateTimeYearEq(cond),
                        searchTextContains(cond.getSplitKeyword())
                )
                .fetchOne();
        return count == null ? 0 : count;
    }

    private static BooleanExpression academyIdEqOrIsNullByPrivateOnly(UUID academyId, boolean privateOnly) {
        BooleanExpression expression = book.academy.id.eq(academyId);
        return privateOnly ? expression : expression.or(book.academy.id.isNull());
    }

    private static BooleanExpression createdDateTimeYearEq(BookPageSearchCond cond) {
        return cond.getYear() == null ? null : book.createdDateTime.year().eq(cond.getYear());
    }

    private static BooleanExpression searchTextContains(String[] keywords) {
        if (keywords == null || keywords.length == 0) {
            return null;
        }
        return Arrays.stream(keywords)
                .map(k -> book.searchText.toLowerCase().contains(k))
                .reduce(BooleanExpression::and)
                .orElse(null);
    }
}
