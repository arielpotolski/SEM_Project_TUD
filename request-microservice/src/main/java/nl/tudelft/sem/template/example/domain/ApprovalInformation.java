package nl.tudelft.sem.template.example.domain;

public class ApprovalInformation {

    private Long[] ids;

    public ApprovalInformation(Long[] ids) {
        this.ids = ids;
    }

    public ApprovalInformation() {
    }

    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }
}
