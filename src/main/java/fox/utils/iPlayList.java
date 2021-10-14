package fox.utils;

import java.nio.file.Path;
import java.util.List;


public interface iPlayList {
    int getRowsCount();
    void selectRow(int rowIndex);
    boolean moveSelectedUp();
    boolean moveSelectedDown();
    void removeSelected();
    void add(List<Path> files);
    Path getTrack(int index);
    boolean isEmpty();
}
