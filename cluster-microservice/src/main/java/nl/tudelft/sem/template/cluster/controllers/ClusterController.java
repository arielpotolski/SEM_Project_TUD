package nl.tudelft.sem.template.cluster.controllers;

import java.time.LocalDate;
import java.util.List;
import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyDatedTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.cluster.JobSchedulingService;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeContributionService;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.models.JobRequestModel;
import nl.tudelft.sem.template.cluster.models.NodeRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ClusterController {

    private final transient AuthManager authManager;

    private final transient NodeRepository nodeRep;
    private final transient JobScheduleRepository jobScheduleRep;

    private final transient JobSchedulingService scheduling;
    private final transient NodeContributionService contribution;

    private final transient DateProvider dateProvider;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public ClusterController(AuthManager authManager, NodeRepository nodeRep,
                             JobScheduleRepository jobScheduleRep, JobSchedulingService scheduling,
                             NodeContributionService contribution, DateProvider dateProvider) {
        this.authManager = authManager;
        this.nodeRep = nodeRep;
        this.jobScheduleRep = jobScheduleRep;
        this.scheduling = scheduling;
        this.contribution = contribution;
        this.dateProvider = dateProvider;
    }

    /**
     * Gets all nodes stored in the cluster.
     *
     * @return a list of containing all the nodes
     */
    @GetMapping("/all")
    public List<Node> getAll() {
        return this.nodeRep.findAll();
    }

    /**
     * Gets a node by the url.
     *
     * @param url the url of the specific node to be found
     * @return the node that has this url. It does not return anything if the url is not found in the cluster
     */
    @GetMapping("/{url}")
    public ResponseEntity<Node> getNode(@PathVariable("url") String url) {
        return ResponseEntity.of(this.nodeRep.findByUrl(url));
    }

    /**
     * Sets up the "central cores" of the existing faculties. They are nodes with 0 of each type of resource which
     * serve as placeholders to ensure that the cluster knows which faculties exist and can, for example, be assigned
     * nodes. This endpoint should be used by the User service as soon as both it and Cluster are online.
     *
     * @param faculties the list of faculties that exist in the User service's database.
     *
     * @return message of confirmation that the faculties have been received.
     */
    @PostMapping("/faculties")
    public ResponseEntity<String> updateOnExistingFaculties(@RequestBody List<String> faculties) {
        for (String faculty : faculties) {
            if (jobScheduleRep.existsByFacultyId(faculty)) {
                continue;
            }
            Node core =  new NodeBuilder()
                                .setNodeCpuResourceCapacityTo(0.0)
                                .setNodeGpuResourceCapacityTo(0.0)
                                .setNodeMemoryResourceCapacityTo(0.0)
                                .withNodeName("FacultyCentralCore")
                                .foundAtUrl("/" + faculty + "/central-core")
                                .byUserWithNetId("SYSTEM")
                                .assignToFacultyWithId(faculty).constructNodeInstance();
            contribution.addNodeAssignedToSpecificFacultyToCluster(core);
        }
        return ResponseEntity.ok("Successfully acknowledged all existing faculties.");
    }

    /**
     * Gets and returns all jobs in the schedule.
     *
     * @return list of all jobs in the schedule.
     */
    @GetMapping("/schedule")
    public List<Job> getSchedule() {
        return this.jobScheduleRep.findAll();
    }

    /**
     * Gets and returns the total amount of resources each faculty has reserved on each day.
     *
     * @return list of FacultyDatedTotalResources, which contain the date, the facultyId, and the amount of resources
     that are reserved for that faculty on that day.
     */
    @GetMapping("/resources/reserved")
    public List<FacultyDatedTotalResources> getReservedPerFacultyPerDay() {
        return this.jobScheduleRep.findResourcesRequiredForEachDay();
    }

    /**
     * Gets and returns the total amount of resources reserved in each faculty for the given day.
     *
     * @param rawDate the String representation of the date in the yyyy-MM-dd format
     *
     * @return a list of FacultyTotalResources, which contain the facultyId and the amount of resources reserved
     * in that faculty on the given day.
     */
    @GetMapping("/resources/reserved/{date}")
    public List<FacultyTotalResources> getReservedPerFacultyForGivenDay(@PathVariable("date") String rawDate) {
        LocalDate dateToFindReservedResourcesFor = LocalDate.parse(rawDate);
        return this.jobScheduleRep.findResourcesRequiredForGivenDay(dateToFindReservedResourcesFor);
    }

    /**
     * Gets and returns the total amount of resources reserved in the given faculty on the given day.
     *
     * @param rawDate the String representation of the date in the yyyy-MM-dd format
     * @param facultyId the facultyId of the faculty to check the reserved resources for.
     *
     * @return ResponseEntity containing a FacultyDatedTotalResources object, which contains the date, the facultyId,
     * and the total reserved resources in each of the three categories.
     */
    @GetMapping("/resources/reserved/{date}/{facultyId}")
    public ResponseEntity<FacultyDatedTotalResources> getReservedForGivenFacultyForGivenDay(
            @PathVariable("date") String rawDate, @PathVariable("facultyId") String facultyId) {
        LocalDate dateToFindReservedResourcesFor = LocalDate.parse(rawDate);
        return ResponseEntity.ok(this.jobScheduleRep
                .findResourcesRequiredForGivenFacultyForGivenDay(dateToFindReservedResourcesFor, facultyId));
    }

    /**
     * Accepts a request from the request service, converts it into a job and schedules it.
     *
     * @param jobModel the JobRequestModel which is the deserialized JSON of the request sent by the Request service.
     *
     * @return a ResponseEntity with an informative message.
     */
    @PostMapping("/request")
    public ResponseEntity<String> forwardRequestToCluster(@RequestBody JobRequestModel jobModel) {
        // extract job from request model
        Job job = new JobBuilder().requestedThroughFaculty(jobModel.getFacultyId())
                                  .requestedByUserWithNetId(jobModel.getUserNetId())
                                  .havingName(jobModel.getJobName())
                                  .withDescription(jobModel.getJobDescription())
                                  .needingCpuResources(jobModel.getRequiredCpu())
                                  .needingGpuResources(jobModel.getRequiredGpu())
                                  .needingMemoryResources(jobModel.getRequiredMemory())
                                  .preferredCompletedBeforeDate(jobModel.getPreferredCompletionDate())
                                  .constructJobInstance();

        // preferred completion date is in the future
        if (job.getPreferredCompletionDate().isBefore(dateProvider.getTomorrow())) {
            return ResponseEntity.badRequest()
                    .body("The requested job cannot require the cluster to compute it before "
                            + dateProvider.getTomorrow() + ".");
        }

        // the resources requested are cpu >= gpu and cpu >= memory
        if (job.getRequiredCpu() < job.getRequiredGpu()
				|| job.getRequiredCpu() < job.getRequiredMemory()) {
            return ResponseEntity.badRequest()
                    .body("The requested job cannot require more GPU or memory than CPU.");
        }

        // can job ever be scheduled
        if (!this.scheduling.checkIfJobCanBeScheduled(job)) {
            return ResponseEntity.badRequest()
                    .body("The requested job requires more resources than are assigned to the "
                            + job.getFacultyId() + " faculty.");
        }

        // schedule job
        scheduling.scheduleJob(job);

        // return
        return ResponseEntity.ok("Successfully scheduled job.");
    }

    /**
     * Adds a new node to the cluster. Fails if amount of cpu resources are not enough
     * or if there is already a node with the same url in the database.
     *
     * @param node the node to be added to the cluster
     * @return A string saying if the node was added. In case of failure, it returns a
     * 			string saying why is it failing
     */
    @PostMapping(path = {"/add"})
    public ResponseEntity<String>  addNode(@RequestBody NodeRequestModel node) {
        // Check if central cores have been installed by User Service
        if ((int) this.nodeRep.count() == 0) {
            // Central cores not installed - faculties unknown. All nodes will be assigned to the
            // Board of Examiners until faculties become known.
            Node core =  new NodeBuilder()
                    .setNodeCpuResourceCapacityTo(0.0)
                    .setNodeGpuResourceCapacityTo(0.0)
                    .setNodeMemoryResourceCapacityTo(0.0)
                    .withNodeName("BoardCentralCore")
                    .foundAtUrl("/board-pf-examiners/central-core")
                    .byUserWithNetId("SYSTEM")
                    .assignToFacultyWithId("Board of Examiners").constructNodeInstance();
            contribution.addNodeAssignedToSpecificFacultyToCluster(core);
        }

        if (this.nodeRep.existsByUrl(node.getUrl())) {
            return ResponseEntity.ok("Failed to add node. A node with this url already exists.");
        }
        String netId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Node n = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(node.getCpuResources())
                .setNodeGpuResourceCapacityTo(node.getGpuResources())
                .setNodeMemoryResourceCapacityTo(node.getMemoryResources())
                .withNodeName(node.getName())
                .foundAtUrl(node.getUrl())
                .byUserWithNetId(netId)
                .constructNodeInstance();

        if (n.hasEnoughCpu().equals("Your node has been successfully added.")) {
            contribution.addNodeToCluster(n);
        }
        return ResponseEntity.ok(n.hasEnoughCpu());
    }

    /**
     * Delete a node from the cluster by url. It does nothing in case the url
     * does not exist.
     *
     * @param url the url by which the node will be found and deleted
     * @return a string saying whether the node was deleted or not
     */
    @DeleteMapping("/{url}")
    public ResponseEntity<String> deleteNode(@PathVariable("url") String url) {
        if (nodeRep.existsByUrl(url)) {
            Node node = nodeRep.findByUrl(url).get();
            this.nodeRep.delete(node);
            return ResponseEntity.ok("The node has been successfully deleted");
        } else {
            return ResponseEntity.ok("Could not find the node to be deleted."
					+ " Check if the url provided is correct.");
        }
    }

    /**
     * Deletes all the nodes from the cluster.
     *
     * @return a string saying if the deletion was successful
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> removeAll() {
        this.nodeRep.deleteAll();
        return ResponseEntity.ok("All nodes have been deleted from the cluster.");
    }

    /**
     * Returns all faculties which have any node assigned to them and the total sum of resources in each of the three
     * categories (CPU, GPU, memory) that they control.
     *
     * @return a list of Spring Projection Interfaces containing the facultyId and the sum of available CPU, GPU, and
     * memory resources for that faculty.
     */
    @GetMapping("/resources/all")
    public List<FacultyTotalResources> getResourcesByFaculty() {
        return this.nodeRep.findTotalResourcesPerFaculty();
    }

    /**
     * Returns the sum of the resources of each of the three categories (CPU, GPU, memory) that the given facultyId
     * has assigned.
     *
     * @param facultyId the facultyId for which to return the assigned resources.
     *
     * @return a Spring Projection Interface containing the facultyId and the sum of available CPU, GPU, and
     * memory resources for that faculty.
     */
    @GetMapping("/resources/all/{facultyId}")
    public ResponseEntity<FacultyTotalResources> getResourcesForGivenFaculty(@PathVariable("facultyId") String facultyId) {
        return ResponseEntity.ok(this.nodeRep.findTotalResourcesForGivenFaculty(facultyId));
    }

    // free resources per day

    // free resources per day for given faculty

}
