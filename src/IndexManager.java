import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IndexManager {
    private Map<String, File> index;
    private FileManager fileManager;

    public IndexManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.index = new HashMap<>();
        buildIndex();
    }

    private void buildIndex() {
        File[] files = fileManager.listFiles();
        if (files != null) {
            for (File file : files) {
                index.put(file.getName(), file);
            }
        }
    }

    public File searchFile(String fileName) {
        return index.get(fileName);
    }

    public void updateIndex() {
        index.clear();
        buildIndex();
    }
}
