package au.id.tmm.hypotheticalsenate.database;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Abstraction of a data source, providing easy methods for downloading, extracting and opening {@link AECResource}s.
 *
 * @author timothy
 */
public class DataSource {

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;

    private AECResource aecResource;
    private File downloadDirectory;

    public DataSource(AECResource aecResource, File downloadDirectory) {
        this.aecResource = aecResource;
        this.downloadDirectory = downloadDirectory;
    }

    public boolean isDownloaded() {
        File file = getExpectedLocalFile();
        return file.exists() && file.isFile();
    }

    private File getExpectedLocalFile() {
        return new File(downloadDirectory, FilenameUtils.getName(this.aecResource.getResourceLocation().getPath()));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void download() {
        try {
            if (!downloadDirectory.isDirectory()) {
                throw new RuntimeException("The download directory is not a directory");
            }

            File localFile = this.getExpectedLocalFile();

            localFile.delete();

            FileUtils.copyURLToFile(this.aecResource.getResourceLocation(), localFile,
                    CONNECTION_TIMEOUT, READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while downloading "
                    + this.aecResource.getResourceLocation().toString(), e);
        }
    }

    public InputStream openInputStream() {
        try {
            if (this.aecResource.isInZip()) {
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
        if (!this.aecResource.getZipEntryName().isPresent()) {
            throw new RuntimeException("A data source expected to be found inside a zip file does not " +
                    "specify the expected name within the archive");
        }

        ZipFile zipFile = new ZipFile(this.getExpectedLocalFile());
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        Optional<ZipEntry> matchingEntry = Optional.empty();

        while(zipEntries.hasMoreElements()) {
            ZipEntry currentEntry = zipEntries.nextElement();
            if (currentEntry.getName().equals(this.aecResource.getZipEntryName().get())) {
                matchingEntry = Optional.of(currentEntry);
                break;
            }
        }

        if (matchingEntry.isPresent()) {
            return zipFile.getInputStream(matchingEntry.get());
        } else {
            throw new RuntimeException("The expected entry " + this.aecResource.getZipEntryName().get()
                    + " was not found in " + "the archive " + zipFile.getName());
        }
    }

    public CSVReader getCSVReader() {
        return new CSVReader(new InputStreamReader(new BufferedInputStream(this.openInputStream())),
                this.aecResource.getCsvSeparator());
    }
}
