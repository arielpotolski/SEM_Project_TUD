package nl.tudelft.sem.template.cluster.domain.cluster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A DDD repository for querying and persisting cluster node information.
 */
@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    /**
     * Tries to find and return a node by node ID.
     *
     * @param id the id to find the node by
     *
     * @return an optional containing the node if found
     */
    Optional<Node> findById(long id);

    /**
     * Checks if a node exists with the given ID.
     *
     * @param id the id to check the existence of
     *
     * @return a boolean indicating whether a node with the given ID exists in the repository
     */
    boolean existsById(long id);

    /**
     * Tries to find and return a node by node url.
     *
     * @param url the url to find the node by
     *
     * @return an optional containing the node if found
     */
    Optional<Node> findByUrl(String url);

    /**
     * Checks if a node exists with the given url.
     *
     * @param url the url to check the existence of
     *
     * @return a boolean indicating whether a node with the given url exists in the repository
     */
    boolean existsByUrl(String url);

}
