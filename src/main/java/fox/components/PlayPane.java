package fox.components;

import fox.out.Out;
import fox.utils.CustomList;
import fox.utils.MyCellRenderer;
import fox.utils.iPlayList;
import gui.BackVocalFrame;
import gui.PlayDataItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class PlayPane extends JPanel implements iPlayList {
    private int indexGlobalCounter = 0;

    private ArrayList<Path> tracks = new ArrayList<>();
    private DefaultListModel<ListRow> dlm = new DefaultListModel();
    private CustomList<ListRow> playList;
    private PlayDataItem owner;


    public PlayPane(PlayDataItem player) {
        this.owner = player;
        setName(player.getName());
        setLayout(new BorderLayout(0,0));
        setOpaque(false);
    }

    private void reload() {
        if (tracks == null || tracks.size() == 0) {
            Out.Print(PlayPane.class, Out.LEVEL.ACCENT, "Tracks array is empty, so return.");
            return;
        }

        removeAll();
        dlm.clear();

        Out.Print(PlayPane.class, Out.LEVEL.DEBUG, "Reloading playlist '" + getName() + "' with " + tracks.size() + " files mp3.");
        indexGlobalCounter = 1;
        File trackIco = new File("./resources/icons/0.png");
        for (Path path : tracks) {
            try {
                dlm.addElement(new ListRow(this, indexGlobalCounter, trackIco, path));
                indexGlobalCounter++;
            } catch (Exception e) {
                e.printStackTrace();
                Out.Print(PlayPane.class, Out.LEVEL.WARN, "Exception by loading track: " + e.getMessage() + ". ('" + path + ";)");
            }
        }

        playList = new CustomList(dlm, PlayPane.this) {
            {
                setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                setFixedCellHeight(32);
//              setVisibleRowCount(5);
                setBackground(Color.DARK_GRAY);
                setCellRenderer(new MyCellRenderer(32));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        BackVocalFrame.updateInfo(playList.getSelectedValue());
                    }
                });
                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                            BackVocalFrame.updateInfo(playList.getSelectedValue());
                        }
                    }
                });
            }
        };

        add(playList, BorderLayout.CENTER);
//        BackVocalFrame.showPlayList(this);
    }

    @Override
    public void add(List<Path> trackPaths) {
        for (Path path : trackPaths) {
            if (Files.exists(path)) {
                tracks.add(path);
            }
        }
        reload();
    }

    @Override
    public Path getTrack(int index) {
        if (index >= dlm.getSize()) {
            return null;
        } else {
            try {
                ListRow el = dlm.getElementAt(index);
                return el.getPath();
            } catch (Exception e) {
                Out.Print(PlayPane.class, Out.LEVEL.WARN, "getTrack(): Exception: " + e.getMessage());
                return null;
            }
        }
    }

    public void setFallTrack(int index) {
        if (index >= dlm.getSize()) {return;}

        try {
            dlm.getElementAt(index).setFall(true);
        } catch (Exception e) {
            Out.Print(PlayPane.class, Out.LEVEL.WARN, "getTrack(): Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isFallTrack(int index) {
        if (index >= dlm.getSize()) {return false;}

        try {
            return dlm.getElementAt(index).isFall();
        } catch (Exception e) {
            return false;
        }
    }

    public int getPlayedIndex() {return owner.getIndexOfPlayed();}

    @Override
    public boolean isEmpty() {
        return dlm.isEmpty();
    }

    @Override
    public int getRowsCount() {
        return dlm.size();
    }

    @Override
    public void selectRow(int rowIndex) {
        playList.setSelectedIndex(rowIndex);
    }


    @Override
    public boolean moveSelectedUp() {
        int index = getSelectedIndex();
        if (index > 0) {
            ListRow tmp = dlm.getElementAt(index);

            dlm.getElementAt(index - 1).setCount(index + 1);
            tmp.setCount(index);

            dlm.removeElementAt(index);
            dlm.insertElementAt(tmp,index - 1);
            playList.setSelectedIndex(index - 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean moveSelectedDown() {
        int index = getSelectedIndex();
        if (index < dlm.size() - 1) {
            ListRow tmp = dlm.getElementAt(index);

            dlm.getElementAt(index).setCount(index + 2);
            dlm.getElementAt(index + 1).setCount(index + 1);

            dlm.removeElementAt(index);
            dlm.insertElementAt(tmp,index + 1);
            playList.setSelectedIndex(index + 1);
            return true;
        }
        return false;
    }

    @Override
    public void removeSelected() {
        tracks.remove(getTrack(getSelectedIndex()));

        for (int i = getSelectedIndex() + 1; i < dlm.size(); i++) {
            dlm.getElementAt(i).setCount(i);
        }

        dlm.removeElementAt(getSelectedIndex());
    }

    public int getSelectedIndex() {
        if (playList == null) {return -1;}

        int si = playList.getSelectedIndex();
        if (si == -1) {
            playList.setSelectedIndex(0);
            si = 0;
        }
        return si;
    }

    public List<Path> getTracks() {
        List<Path> result = new ArrayList<>();
        for (int i = 0; i < dlm.size(); i++) {
            result.add(dlm.get(i).getPath());
        }
        return result;
    }

    public void setTracks(List<Path> tracks) {
        try {
            List<Path> result = new ArrayList<>();

            for (Path file : tracks) {
                if (Files.isRegularFile(file)) {
                    if (file.toFile().getName().endsWith(".mp3")) {
                        result.add(file);
                    }
                }
            }

            add(result);
        } catch (Exception e) {
            Out.Print(PlayPane.class, Out.LEVEL.WARN, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setTracks(File dir) {
        try {
            List<Path> result = new ArrayList<>();

            for (File file : dir.listFiles()) {
                if (Files.isRegularFile(file.toPath())) {
                    if (file.getName().endsWith(".mp3")) {
                        result.add(file.toPath());
                    }
                }
            }

            add(result);
        } catch (Exception e) {
            Out.Print(PlayPane.class, Out.LEVEL.WARN, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearTracks() {
        tracks.clear();
        dlm.clear();
    }

    public PlayDataItem getOwner() {return owner;}

    public boolean isAlarmSounded() {
        return owner.isAlarmPlayed();
    }
}
