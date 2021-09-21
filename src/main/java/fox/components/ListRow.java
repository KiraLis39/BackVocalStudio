package fox.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;


public class ListRow extends JPanel implements Comparator<ListRow> {
    private BufferedImage icon;
    private String text;
    private Path trackPath;
    private int count;
    private PlayPane owner;

    public ListRow(PlayPane owner, int count, File iconFile, Path trackPath) {
        try {icon = ImageIO.read(iconFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.trackPath = trackPath;
        this.owner = owner;
        this.text = trackPath.toFile().getName();
        this.count = count;
    }

    public BufferedImage getImIcon() {return icon;}

    public int getCount() {return count;}
    public void setCount(int count) {
        this.count = count;
    }

    public String getListItemText() { return text; }

    @Override
    public int compare(ListRow o1, ListRow o2) {
        return o1.count > o2.count ? 1 : -1;
    }

    public Path getPath() {
        return trackPath;
    }
}