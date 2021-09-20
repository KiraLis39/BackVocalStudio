package core;

import fox.components.ListItem;
import fox.components.MyCellRenderer;
import fox.out.Out;
import gui.BackVocalFrame;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;


public class Playlist extends JPanel implements iPlayList {
    private List<Path> musicFilesList;
    private DefaultListModel<ListItem> dlm = new DefaultListModel<>();
    private JList<String> playList;

    public Playlist(PlayDateItem player, List<Path> musicFilesList) {
        this.musicFilesList = musicFilesList;

        setName(player.getName());
        setLayout(new BorderLayout(3,3));
        setOpaque(false);

        reload();
    }

    private void reload() {
        if (musicFilesList == null || musicFilesList.size() == 0) {return;}
        Out.Print("Reloading playlist...");

        removeAll();
        dlm.clear();

        System.out.println("IN DIR HAS " + musicFilesList.size() + " files mp3.");
        for (Path file : musicFilesList) {
            System.out.println("Adding to pl: " + file.toFile().getName());
            add(file.toFile().getName());
        }

        playList = new JList(dlm);
        playList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//      playList.setLayoutOrientation(JList.VERTICAL);
        playList.setFixedCellHeight(32);
        playList.setBackground(Color.DARK_GRAY);
        playList.setCellRenderer(new MyCellRenderer(32));

        add(playList);

        BackVocalFrame.showPlayList(this);
    }


    @Override
    public Path getTrack(int index) {
        return musicFilesList.get(index);
    }

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
    public void moveSelectedUp() {

    }

    @Override
    public void moveSelectedDown() {

    }

    @Override
    public void removeSelected() {

    }

    @Override
    public void add(String fileName) {
        dlm.addElement(new ListItem(new File("./resources/icons/0.png"), fileName));
    }

    public int getSelectedIndex() {
        int si = playList.getSelectedIndex();
        if (si == -1) {
            playList.setSelectedIndex(0);
            si = 0;
        }
        return si;
    }
}
