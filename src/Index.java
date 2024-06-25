import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Index {
    private Map<String, File> index = new HashMap<>();

    public Index(String sharedFolder) {
        updateIndexFromFolder(new File(sharedFolder));
    }

    public void updateIndex(String fileName, File file) {
        index.put(fileName, file);
    }

    public File searchFile(String fileName) {
        System.out.println("Buscando archivo en el índice: " + fileName);
        fileName = fileName.trim(); // Remover espacios en blanco alrededor
        File file = index.get(fileName);
        if (file != null && file.exists()) {
            System.out.println("Archivo encontrado en el índice: " + fileName);
        } else {
            System.out.println("Archivo no encontrado en el índice: " + fileName);
        }
        return file;
    }

    private void updateIndexFromFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        updateIndex(file.getName(), file);
                        System.out.println("Archivo indexado: " + file.getName());
                    }
                }
            }
        }
    }

    public Map<String, File> getIndex() {
        return index;
    }
}
