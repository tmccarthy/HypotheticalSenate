package au.id.tmm.hypotheticalsenate.database;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author timothy
 */
public enum DataSource {

    SENATE_CANDIDATES("http://results.aec.gov.au/17496/Website/Downloads/SenateCandidatesDownload-17496.txt"),
    GROUP_VOTING_TICKETS("http://results.aec.gov.au/17496/Website/Downloads/SenateGroupVotingTicketsDownload-17496.txt")

    ;

    public static final String DOWNLOADS_LOCATION = "AEC_DATA/";

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;

    private URL downloadLocation;
    private boolean inZip;
    private Optional<String> zipEntryName = Optional.empty();
    private char csvSeparator;

    private DataSource(String downloadLocation, boolean inZip, @Nullable String zipEntryName, char csvSeparator) {
        try {
            this.downloadLocation = new URL(downloadLocation);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.inZip = inZip;
        this.zipEntryName = Optional.ofNullable(zipEntryName);
        this.csvSeparator = csvSeparator;
    }

    private DataSource(String downloadLocation, char csvSeparator) {
        this(downloadLocation, false, null, csvSeparator);
    }

    private DataSource(String downloadLocation) {
        this(downloadLocation, '\t');
    }

    public boolean isDownloaded() {
        File file = getExpectedLocalFile();
        return file.exists() && file.isFile();
    }

    private File getExpectedLocalFile() {
        return new File(DOWNLOADS_LOCATION, FilenameUtils.getName(downloadLocation.getPath()));
    }

    public void download() {
        try {
            File localFile = this.getExpectedLocalFile();

            localFile.delete();

            FileUtils.copyURLToFile(this.downloadLocation, localFile, CONNECTION_TIMEOUT, READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while downloading " + this.downloadLocation.toString(), e);
        }
    }

    public InputStream openInputStream() {
        try {
            if (this.inZip) {
                return inputStreamFromZip();
            } else {
                return new FileInputStream(this.getExpectedLocalFile());
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while opening " +
                    this.getExpectedLocalFile().getAbsolutePath(), e);
        }
    }

    private InputStream inputStreamFromZip() throws IOException {
        if (!this.zipEntryName.isPresent()) {
            throw new RuntimeException("A data source expected to be found inside a zip file does not " +
                            "specify the expected name within the archive");
        }

        ZipFile zipFile = new ZipFile(this.getExpectedLocalFile());
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        Optional<ZipEntry> matchingEntry = Optional.empty();

        while(zipEntries.hasMoreElements()) {
            ZipEntry currentEntry = zipEntries.nextElement();
            if (currentEntry.getName().equals(this.zipEntryName.get())) {
                matchingEntry = Optional.of(currentEntry);
                break;
            }
        }

        if (matchingEntry.isPresent()) {
            return zipFile.getInputStream(matchingEntry.get());
        } else {
            throw new RuntimeException("The expected entry " + this.zipEntryName.get() + " was not found in " +
                    "the archive " + zipFile.getName());
        }
    }

    public CSVReader getCSVReader() {
        return new CSVReader(new InputStreamReader(new BufferedInputStream(this.openInputStream())), this.csvSeparator);
    }
}
