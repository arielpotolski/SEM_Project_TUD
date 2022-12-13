package nl.tudelft.sem.template.cluster.controllers;

import java.time.LocalDate;
import java.util.List;
import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.services.JobSchedulingService;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.domain.services.NodeInformationAccessingService;
import nl.tudelft.sem.template.cluster.domain.services.SchedulerInformationAccessingService;
import nl.tudelft.sem.template.cluster.models.JobRequestModel;
import nl.tudelft.sem.template.cluster.models.NodeRequestModel;
import nl.tudelft.sem.template.cluster.models.TotalResourcesResponseModel;
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

    private final transient JobSchedulingService scheduling;
    private final transient NodeContributionService nodeContributionService;
    private final transient NodeInformationAccessingService nodeInformationAccessingService;
    private final transient SchedulerInformationAccessingService schedulerInformationAccessingService;

    private final transient DateProvider dateProvider;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public ClusterController(AuthManager authManager, JobSchedulingService scheduling,
                             NodeContributionService nodeContributionService, DateProvider dateProvider,
                             NodeInformationAccessingService nodeInformationAccessingService,
                             SchedulerInformationAccessingService schedulerInformationAccessingService) {
        this.authManager = authManager;
        this.scheduling = scheduling;
        this.nodeContributionService = nodeContributionService;
        this.dateProvider = dateProvider;
        this.nodeInformationAccessingService = nodeInformationAccessingService;
        this.schedulerInformationAccessingService = schedulerInformationAccessingService;
    }

    /**
     * Provides an endpoint for accessing nodes directly. This can be either all nodes or a node at specified url.
     *
     * @param url the url to look for the node at (if given).
     *
     * @return response entity containing the list of all relevant nodes (or the looked for node when url provided and
     * exists in the database).
     */
    @GetMapping(value = {"/nodes", "/nodes/{url}"})
    public ResponseEntity<List<Node>> getNodeInformation(@PathVariable(value = "url", required = false) String url) {
        if (url == null) {
            return ResponseEntity.ok(this.nodeInformationAccessingService.getAllNodes());
        } else if (this.nodeInformationAccessingService.existsByUrl(url)) {
            return ResponseEntity.ok(List.of(this.nodeInformationAccessingService.getByUrl(url)));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Adds a new node to the cluster. Fails if amount of cpu resources are not enough
     * or if there is already a node with the same url in the database.
     *
     * @param node the node to be added to the cluster
     * @return A string saying if the node was added. In case of failure, it returns a
     * 			string saying why is it failing
     */
    @PostMapping(path = {"/nodes/add"})
    public ResponseEntity<String>  addNode(@RequestBody NodeRequestModel node) {
        // Check if central cores have been installed by User Service
        if (this.nodeInformationAccessingService.getNumberOfNodesInRepository() == 0) {
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
            this.nodeContributionService.addNodeAssignedToSpecificFacultyToCluster(core);
        }

        if (this.nodeInformationAccessingService.existsByUrl(node.getUrl())) {
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
            this.nodeContributionService.addNodeToCluster(n);
        }
        return ResponseEntity.ok(n.hasEnoughCpu());
    }

    /**
     * Delete all nodes or specified node, if url provided.
     *
     * @param url the url by which the node will be found and deleted, if provided.
     *
     * @return a string saying whether the node(s) was deleted or not
     */
    @DeleteMapping(value = {"/nodes/delete", "/nodes/delete/{url}"})
    public ResponseEntity<String> deleteNode(@PathVariable(value = "url", required = false) String url) {
        if (url == null) {
            this.nodeInformationAccessingService.deleteAllNodes();
            return ResponseEntity.ok("All nodes have been deleted from the cluster.");
        }

        if (this.nodeInformationAccessingService.existsByUrl(url)) {
            Node node = this.nodeInformationAccessingService.getByUrl(url);
            this.nodeInformationAccessingService.deleteNode(node);
            return ResponseEntity.ok("The node has been successfully deleted");
        } else {
            return ResponseEntity.ok("Could not find the node to be deleted."
                    + " Check if the url provided is correct.");
        }
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
            if (this.schedulerInformationAccessingService.existsByFacultyId(faculty)) {
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
            this.nodeContributionService.addNodeAssignedToSpecificFacultyToCluster(core);
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
        return this.schedulerInformationAccessingService.getAllJobsFromSchedule();
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
        if (job.getPreferredCompletionDate().isBefore(this.dateProvider.getTomorrow())) {
            return ResponseEntity.badRequest()
                    .body("The requested job cannot require the cluster to compute it before "
                            + this.dateProvider.getTomorrow() + ".");
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
        this.scheduling.scheduleJob(job);

        // return
        return ResponseEntity.ok("Successfully scheduled job.");
    }

    /**
     * Gets and returns the total amount of resources in each of the three categories (CPU, GPU, memory) per faculty.
     * If facultyId provided as path parameter, returns only the resources assigned to specific faculty, if it exists
     * in the database.
     *
     * @param facultyId the facultyId to get the assigned resources to.
     *
     * @return response entity containing a list of all relevant resources with facultyIds (or one FacultyTotalResource
     * object if facultyId specified).
     */
    @GetMapping(value = {"/resources/assigned", "/resources/assigned/{facultyId}"})
    public ResponseEntity<List<FacultyTotalResources>> getResourcesAssignedToFaculty(
            @PathVariable(value = "facultyId", required = false) String facultyId) {
        if (facultyId == null) {
            return ResponseEntity.ok(this.nodeInformationAccessingService.getAssignedResourcesPerFaculty());
        } else if (this.nodeInformationAccessingService.existsByFacultyId(facultyId)) {
            return ResponseEntity.ok(List.of(this.nodeInformationAccessingService
                    .getAssignedResourcesForGivenFaculty(facultyId)));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets and returns the total reserved resources per day per faculty. Uses TotalResourcesInterface to encapsulate
     * different return types: DatedTotalResources, returned when grouping by date; FacultyTotalResources, returned when
     * grouping by facultyId; and FacultyDatedTotalResources, returned when not grouping and when only looking for
     * resources reserved for a specific faculty and date.
     *
     * @param rawDate the date on which to look for reserved resources, in String format.
     * @param facultyId the facultyId to find reserved resources of.
     *
     * @return response entity containing a list of Spring Projection Interfaces containing the reserved resources
     * in the three categories, as well as the date, the facultyId, or both.
     */
    @GetMapping(value = {"/resources/reserved", "/resources/reserved/{date}&{facultyId}",
        "/resources/reserved/{date}&", "/resources/reserved/&{facultyId}", "resources/reserved/&"})
    public ResponseEntity<List<TotalResourcesResponseModel>> getReservedResourcesPerFacultyPerDay(
            @PathVariable(value = "date", required = false) String rawDate,
            @PathVariable(value = "facultyId", required = false) String facultyId) {
        LocalDate date = LocalDate.parse(rawDate);
        if (date == null && facultyId == null) {
            // for all dates, for all faculties
            return ResponseEntity.ok(this.schedulerInformationAccessingService
                            .convertToResponseModels(this.schedulerInformationAccessingService
                            .getReservedResourcesPerFacultyPerDay()));
        } else if (date != null && facultyId == null) {
            if (!this.schedulerInformationAccessingService.existsByScheduledFor(date)) {
                return ResponseEntity.badRequest().build();
            }

            // for given date, for all faculties
            return ResponseEntity.ok(this.schedulerInformationAccessingService
                    .convertToResponseModels(this.schedulerInformationAccessingService
                            .getReservedResourcesPerFacultyForGivenDay(date)));
        } else if (date == null && facultyId != null) {
            if (!this.schedulerInformationAccessingService.existsByFacultyId(facultyId)) {
                return ResponseEntity.badRequest().build();
            }

            // for given faculty, for all dates
            return ResponseEntity.ok(this.schedulerInformationAccessingService
                    .convertToResponseModels(this.schedulerInformationAccessingService
                            .getReservedResourcesPerDayForGivenFaculty(facultyId)));
        } else {
            if (!this.schedulerInformationAccessingService.existsByScheduledFor(date)
                || !this.schedulerInformationAccessingService.existsByFacultyId(facultyId)) {
                return ResponseEntity.badRequest().build();
            }

            // for given faculty, for given date
            return ResponseEntity.ok(this.schedulerInformationAccessingService
                    .convertToResponseModels(this.schedulerInformationAccessingService
                            .getReservedResourcesForGivenDayForGivenFaculty(date, facultyId)));
        }
    }

    // free resources per day

    // free resources per day for given faculty

}
