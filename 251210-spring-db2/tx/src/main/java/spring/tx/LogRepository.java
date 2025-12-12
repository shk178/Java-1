package spring.tx;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LogRepository {
    private final EntityManager em;
    @Transactional
    public void save(Log log) {
        em.persist(log);
        if (log.getMessage().contains("re")) {
            throw new RuntimeException(log.getMessage());
        }
    }

    public Optional<Log> find(String message) {
        return em.createQuery("select l from Log l where l.message = :message")
                .setParameter("message", message)
                .getResultList()
                .stream()
                .findAny();
    }
}
