import java.io.File;
import java.util.Map;

public class LocalIndexSearch {
    private Index index;  // Índice del nodo

    public LocalIndexSearch(Index index) {
        this.index = index;  // Asigna el índice
    }

    public File searchLocal(String fileName) {
        return index.searchFile(fileName);  // Busca el archivo en el índice local
    }

    public Map<String, File> getIndex() {
        return index.getIndex();  // Devuelve el mapa del índice
    }

}
