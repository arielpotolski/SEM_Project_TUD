package nl.tudelft.sem.template.cluster.domain.cluster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A DDD repository for querying and persisting cluster node information.
 */
@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findById(long id);

    boolean existsById(long id);

    Optional<Node> findByUrl(String url);

    boolean existsByUrl(String url);

}
