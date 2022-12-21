package nl.tudelft.sem.template.cluster.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataProcessingService {

    @Autowired
    public DataProcessingService() {
    }



    // get all existing faculties, check if in DB - remove if no
    // for each faculty, calculate available resources for all days until last day in schedule + 1
    // add methods to filter result, e.g., return for all faculties for all days, for given day for all faculties, and
    // for given faculty for given day

}
