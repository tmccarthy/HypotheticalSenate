package au.id.tmm.hypotheticalsenate.database;

import au.id.tmm.hypotheticalsenate.model.AustralianState;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Enumeration of resources from the AEC used in this project.
 *
 * @author timothy
 */
public enum AECResource {

    SENATE_CANDIDATES("http://results.aec.gov.au/17496/Website/Downloads/" +
            "SenateCandidatesDownload-17496.txt"),
    GROUP_VOTING_TICKETS("http://results.aec.gov.au/17496/Website/Downloads/" +
            "SenateGroupVotingTicketsDownload-17496.txt"),
    GROUP_FIRST_PREFERENCES("http://results.aec.gov.au/17496/Website/Downloads/" +
            "SenateUseOfGvtByGroupDownload-17496.txt"),
    ACT_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-ACT.zip",
            "SenateStateBTLPreferences-17496-ACT.txt"),
    NSW_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-NSW.zip",
            true,
            "SenateStateBTLPreferences-17496-NSW.csv", ','),
    NT_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-NT.zip",
            "SenateStateBTLPreferences-17496-NT.txt"),
    QLD_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-QLD.zip",
            "SenateStateBTLPreferences-17496-QLD.txt"),
    SA_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-SA.zip",
            "SenateStateBTLPreferences-17496-SA.txt"),
    TAS_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-TAS.zip",
            "SenateStateBTLPreferences-17496-TAS.txt"),
    VIC_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-VIC.zip",
            "SenateStateBTLPreferences-17496-VIC.txt"),
    WA_BTL_PREFERENCES("http://results.aec.gov.au/17496/Website/External/" +
            "SenateStateBtlDownload-17496-WA.zip",
            "SenateStateBTLPreferences-17496-WA.txt")
    ;

    public static final ImmutableMap<AustralianState, AECResource> BTL_DATA_MAP =
            ImmutableMap.<AustralianState, AECResource>builder()
                    .put(AustralianState.ACT, ACT_BTL_PREFERENCES)
                    .put(AustralianState.NSW, NSW_BTL_PREFERENCES)
                    .put(AustralianState.NT, NT_BTL_PREFERENCES)
                    .put(AustralianState.QLD, QLD_BTL_PREFERENCES)
                    .put(AustralianState.SA, SA_BTL_PREFERENCES)
                    .put(AustralianState.TAS, TAS_BTL_PREFERENCES)
                    .put(AustralianState.VIC, VIC_BTL_PREFERENCES)
                    .put(AustralianState.WA, WA_BTL_PREFERENCES)
                    .build();

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
