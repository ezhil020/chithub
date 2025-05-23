package chitFund.domain;

public class ChitGroup {
    private int id;
    private String name;
    private double schemeAmount;
    private int duration;
    private double monthlyDue;

    public ChitGroup(int id, String name, double schemeAmount, int duration, double monthlyDue, String customers) {
        this.id = id;
        this.name = name;
        this.schemeAmount = schemeAmount;
        this.duration = duration;
        this.monthlyDue = monthlyDue;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSchemeAmount() {
        return schemeAmount;
    }

    public void setSchemeAmount(double schemeAmount) {
        this.schemeAmount = schemeAmount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getMonthlyDue() {
        return monthlyDue;
    }

    public void setMonthlyDue(double monthlyDue) {
        this.monthlyDue = monthlyDue;
    }

    @Override
    public String toString() {
        return "ChitGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", schemeAmount=" + schemeAmount +
                ", duration=" + duration +
                ", monthlyDue=" + monthlyDue +
                '}';
    }
}