package ua.edu.nupp.kpp.server;

/** Лісова ділянка (таблиця forest_plot). sensitivityScore використовується як фактор F3. */
public class ForestPlot {

    private Long id;
    private String code;
    private String name;
    private String forestry;
    private double areaHa;
    private double sensitivityScore;

    public ForestPlot() {
    }

    public ForestPlot(Long id, String code, String name, String forestry,
                      double areaHa, double sensitivityScore) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.forestry = forestry;
        this.areaHa = areaHa;
        this.sensitivityScore = sensitivityScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForestry() {
        return forestry;
    }

    public void setForestry(String forestry) {
        this.forestry = forestry;
    }

    public double getAreaHa() {
        return areaHa;
    }

    public void setAreaHa(double areaHa) {
        this.areaHa = areaHa;
    }

    public double getSensitivityScore() {
        return sensitivityScore;
    }

    public void setSensitivityScore(double sensitivityScore) {
        this.sensitivityScore = sensitivityScore;
    }
}
