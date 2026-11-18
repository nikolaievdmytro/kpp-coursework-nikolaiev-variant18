package ua.edu.nupp.kpp.server;

/** Інспекційне завдання (таблиця inspection_task), пов'язане з порушенням. */
public class InspectionTask {

    private Long id;
    private long violationId;
    private String description;
    private String plannedDate;

    public InspectionTask() {
    }

    public InspectionTask(Long id, long violationId, String description, String plannedDate) {
        this.id = id;
        this.violationId = violationId;
        this.description = description;
        this.plannedDate = plannedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getViolationId() {
        return violationId;
    }

    public void setViolationId(long violationId) {
        this.violationId = violationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }
}
