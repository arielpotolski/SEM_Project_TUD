package nl.tudelft.sem.template.example.domain;

/**
 * The type Approval information.
 */
public class ApprovalInformation {

    private Long[] ids;

    /**
     * Instantiates a new Approval information.
     *
     * @param ids the ids
     */
    public ApprovalInformation(Long[] ids) {
        this.ids = ids;
    }

    /**
     * Instantiates a new Approval information.
     */
    public ApprovalInformation() {
    }

    /**
     * Get ids long [ ].
     *
     * @return the long [ ]
     */
    public Long[] getIds() {
        return ids;
    }

    /**
     * Sets ids.
     *
     * @param ids the ids
     */
    public void setIds(Long[] ids) {
        this.ids = ids;
    }
}
