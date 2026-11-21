package ua.edu.nupp.kpp.client;

/** Клієнтська модель спостереження: дані, які сервер повертає у JSON. Логіки не містить. */
public class Observation {

    private Long id;
    private long plotId;
    private String observationType;
    private double affectedAreaHa;
    private double severityScore;

    public Observation() {
    }

    public Observation(Long id, long plotId, String observationType,
                       double affectedAreaHa, double severityScore) {
        this.id = id;
        this.plotId = plotId;
        this.observationType = observationType;
        this.affectedAreaHa = affectedAreaHa;
        this.severityScore = severityScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getPlotId() {
        return plotId;
    }

    public void setPlotId(long plotId) {
        this.plotId = plotId;
    }

    public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    public double getAffectedAreaHa() {
        return affectedAreaHa;
    }

    public void setAffectedAreaHa(double affectedAreaHa) {
        this.affectedAreaHa = affectedAreaHa;
    }

    public double getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(double severityScore) {
        this.severityScore = severityScore;
    }
}
