import java.io.File;

public class FileManager {
    private String sharedFolderPath;

    public FileManager(String sharedFolderPath) {
        this.sharedFolderPath = sharedFolderPath;
        createSharedFolderIfNotExists();
    }

    private void createSharedFolderIfNotExists() {
        File folder = new File(sharedFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public File[] listFiles() {
        File folder = new File(sharedFolderPath);
        return folder.listFiles();
    }

    public File getFile(String fileName) {
        File file = new File(sharedFolderPath + File.separator + fileName);
        if (file.exists()) {
            return file;
        }
        return null;
    }
}
