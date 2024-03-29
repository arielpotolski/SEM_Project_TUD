package nl.tudelft.sem.template.example.domain;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByApprovedIs(Boolean bool);


}
