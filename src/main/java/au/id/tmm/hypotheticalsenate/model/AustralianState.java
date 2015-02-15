package au.id.tmm.hypotheticalsenate.model;

/**
 * @author timothy
 */
public enum AustralianState {

    ACT("ACT", "Australian Capital Territory", 2, true),
    NSW("NSW", "New South Wales", 6),
    NT("NT", "Northern Territory", 2, true),
    QLD("QLD", "Queensland", 6),
    SA("SA", "South Australia", 6),
    TAS("TAS", "Tasmania", 6),
    VIC("VIC", "Victoria", 6),
    WA("WA", "Western Australia", 6),
    ;

    private final String code;
    private final String name;
    private final int normalVacancies;
    private final boolean definiteArticle;

    private AustralianState(String code, String name, int normalVacancies, boolean definiteArticle) {
        this.code = code;
        this.name = name;
        this.normalVacancies = normalVacancies;

        this.definiteArticle = definiteArticle;
    }

    private AustralianState(String code, String name, int normalVacancies) {
        this(code, name, normalVacancies, false);
    }

    public int getNormalVacancies() {
        return normalVacancies;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String render() {
        return (this.definiteArticle ? "the " : "") + this.name;
    }

    @Override
    public String toString() {
        return code;
    }
}
