package au.id.tmm.hypotheticalsenate.model;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Enum describing the different Senate elections for which data is available.
 *
 * @author timothy
 */
public enum Election {

    ELECTION_2014_WA(17875, new GregorianCalendar(5, 4, 2014).getTime(), "2014 WA Senate Special Election"),
    ELECTION_2013(17496, new GregorianCalendar(7, 11, 2013).getTime(), "2013 Federal Election"),
    ELECTION_2010(15508, new GregorianCalendar(21, 8, 2010).getTime(), "2010 Federal Election"),
    ELECTION_2007(13745, new GregorianCalendar(24, 11, 2007).getTime(), "2007 Federal Election");

    private int id;
    private Date date;
    private String description;

    private Election(int id, Date date, String description) {
        this.id = id;
        this.date = date;
        this.description = description;
    }

    public int getID() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
