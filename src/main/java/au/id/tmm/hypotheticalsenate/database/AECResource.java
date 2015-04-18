package au.id.tmm.hypotheticalsenate.database;

import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Enumeration of resources from the AEC used in this project.
 *
 * @author timothy
 */
public class AECResource {

    public static AECResource candidates(Election election) {
        return new AECResource("http://results.aec.gov.au/" + election.getID() + "/Website/Downloads/SenateCandidatesDownload-" + election.getID() + ".txt");
    }

    public static AECResource groupVotingTickets(Election election) {
        return new AECResource("http://results.aec.gov.au/" + election.getID() + "/Website/Downloads/SenateGroupVotingTicketsDownload-" + election.getID() + ".txt");
    }

    public static AECResource groupFirstPreferences(Election election) {
        return new AECResource("http://results.aec.gov.au/" + election.getID() + "/Website/Downloads/SenateUseOfGvtByGroupDownload-" + election.getID() + ".txt");
    }

    public static AECResource btlPreferences(Election election, AustralianState state) {
        return new AECResource("http://results.aec.gov.au/" + election.getID() + "/Website/External/SenateStateBtlDownload-" + election.getID() + "-" + state.getCode() + ".zip",
                true,
                "SenateStateBTLPreferences-" + election.getID() + "-" + state.getCode() + ".csv",
                ',');
    }

    private URL resourceLocation;
    private boolean inZip;
    private Optional<String> zipEntryName = Optional.empty();
    private char csvSeparator;

    private AECResource(String resourceLocation, boolean inZip, @Nullable String zipEntryName, char csvSeparator) {
        try {
            this.resourceLocation = new URL(resourceLocation);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.inZip = inZip;
        this.zipEntryName = Optional.ofNullable(zipEntryName);
        this.csvSeparator = csvSeparator;
    }

    private AECResource(String resourceLocation, boolean inZip, @Nullable String zipEntryName) {
        this(resourceLocation, inZip, zipEntryName, '\t');
    }

    private AECResource(String resourceLocation, String zipEntryName) {
        this(resourceLocation, true, zipEntryName);
    }

    private AECResource(String resourceLocation, char csvSeparator) {
        this(resourceLocation, false, null, csvSeparator);
    }

    private AECResource(String resourceLocation) {
        this(resourceLocation, '\t');
    }

    public URL getResourceLocation() {
        return resourceLocation;
    }

    public boolean isInZip() {
        return inZip;
    }

    public Optional<String> getZipEntryName() {
        return zipEntryName;
    }

    public char getCsvSeparator() {
        return csvSeparator;
    }
}
