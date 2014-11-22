package au.id.tmm.hypotheticalsenate.model;

/**
 * @author timothy
 */
public enum AustralianState {

    ACT("ACT", "Australian Capital Territory"),
    NSW("NSW", "New South Wales"),
    NT("NT", "Northern Territory"),
    QLD("QLD", "Queensland"),
    SA("SA", "South Australia"),
    TAS("TAS", "Tasmania"),
    VIC("VIC", "Victoria"),
    WA("WA", "Western Australia"),

    ;
    private final String code;
    private final String name;

    private AustralianState(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code;
    }
}
