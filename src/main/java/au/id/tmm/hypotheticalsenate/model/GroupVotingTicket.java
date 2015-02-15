package au.id.tmm.hypotheticalsenate.model;

import gnu.trove.map.TIntIntMap;

/**
 * @author timothy
 */
public class GroupVotingTicket {

    private String ownerGroup;
    private int ticketNum;
    private TIntIntMap preferences;

    public GroupVotingTicket(String ownerGroup, int ticketNum, TIntIntMap preferences) {
        this.ownerGroup = ownerGroup;
        this.ticketNum = ticketNum;
        this.preferences = preferences;
    }

    public String getOwnerGroup() {
        return ownerGroup;
    }

    public int getTicketNum() {
        return ticketNum;
    }

    public TIntIntMap getPreferences() {
        return preferences;
    }
}
