package gui;

import door.DayCore;
import door.MainClass;
import fox.components.ListRow;
import fox.components.PlayPane;
import fox.fb.FoxFontBuilder;
import fox.ia.InputAction;
import fox.out.Out;
import fox.render.FoxRender;
import registry.Codes;
import registry.Registry;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public class BackVocalFrame extends JFrame implements WindowListener, ComponentListener, ActionListener {
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static ImageIcon ico;

    private static BackVocalFrame frame;
    private static JPanel basePane, centerPlaylistsPane, playDatePane, downBtnsPane, downShedulePane, rightInfoPane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn, moveUpBtn, moveDownBtn, removeBtn, addTrackBtn, showInfoBtn;
    private static JLabel nowPlayedLabel, currentTime, selTrackName, selTrackPath, selTrackDuration, selTrackSize;
    private static JProgressBar playProgress;
    private static JFileChooser fch = new JFileChooser("./resources/audio/");
    private static JToolBar toolBar;

    private static int daysCounter = 0, maxDownPaneHeight = 220;
    private static boolean isInfoShowed;
    private static ActionListener alist;

    private static String alSt1 = "*** Воспроизведение оповещения ***";
    private static String alSt2 = "(жми стоп для прерывания)";
    private static float alF1, alF2;
    private static BufferedImage alarmInfo;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (getSelectedItem() == null) {return;}
        if (getSelectedItem().isAlarmPlayed()) {
            if (alarmInfo == null) {
                rebuildAlIn();
            }

            g.drawImage(alarmInfo, getWidth() / 2 - alarmInfo.getWidth() / 2, getHeight() / 3, BackVocalFrame.this);
        }
    }

    private void rebuildAlIn() {
        alarmInfo = new BufferedImage(getWidth() / 3, getHeight() / 6, BufferedImage.TYPE_INT_ARGB);

        Graphics g = alarmInfo.createGraphics();
        FoxRender.setMedRender((Graphics2D) g);
        g.setFont(Registry.alarmFont0);

        alF1 = FoxFontBuilder.getStringWidth(g, alSt1).intValue();
        alF2 = FoxFontBuilder.getStringWidth(g, alSt2).intValue();

        int y = alarmInfo.getHeight() / 2;
        int x1 = (int) (alarmInfo.getWidth() / 2 - alF1 / 2);
        int x2 = (int) (alarmInfo.getWidth() / 2 - alF2 / 2);

        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(1,1, alarmInfo.getWidth() - 2, alarmInfo.getHeight() - 2,16, 16);

        g.setColor(Color.GRAY);
        g.drawString(alSt1,x1 - 2, y - 11);
        g.drawString(alSt2,x2 - 2, y + 11);

        g.setColor(Color.WHITE);
        g.drawString(alSt1, x1, y - 12);
        g.drawString(alSt2, x2, y + 12);

        g.dispose();
    }

    public BackVocalFrame() {
        frame = this;
        alist = this;

        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Build the frame...");

        try {
            ico = new ImageIcon(ImageIO.read(new File("./resources/icons/0.png")));
            setIconImage(ico.getImage());
        } catch (IOException e) {
            Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Ico image can`t load." + e.getMessage());
            e.printStackTrace();
        }
        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        basePane = new JPanel(new BorderLayout(1, 3)) {
            {
                setBackground(Color.BLACK);

                centerPlaylistsPane = new JPanel(new BorderLayout(3, 3)) {
                    {setOpaque(false);}
                };
                playListsScroll = new JScrollPane(centerPlaylistsPane) {
                    {
                        setBorder(null);
                        getViewport().setBorder(null);
                        getViewport().setBackground(Color.BLACK);
                        setBackground(Color.BLACK);
                        getViewport().setForeground(Color.WHITE);
                        setOpaque(false);
                        getVerticalScrollBar().setUnitIncrement(18);
                        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    }
                };
                rightInfoPane = new RightInfoPanel();
                downShedulePane = new DownSchedulePanel();

                add(playListsScroll, BorderLayout.CENTER);
                add(rightInfoPane, BorderLayout.EAST);
                add(downShedulePane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        addWindowListener(this);
        addComponentListener(this);

        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Show the frame...");
        setVisible(true);
        rebuildAlIn();

        loadDays();

        Component ei = DayCore.getDayItem(0);
        while (ei.getWidth() <= 10) {
            if (ei.getWidth() == 0) {Thread.currentThread().yield();}
        }
        setMinimumSize(new Dimension(ei.getWidth() * 7 + 6, 600));
        setLocationRelativeTo(null);

        InputAction.add("frame", frame);
        InputAction.set("frame", "deleteRow", KeyEvent.VK_DELETE, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getID() == 1001) {
                    removeRow();
                }
            }
        });

        DayCore.execute();
    }

    public static void resetDownPaneSelect() {
        for (PlayDataItem comp : getWeekdayItems()) {
            comp.setSelected(false);
        }
    }

    public static void showPlayList(PlayPane playpane) {
        centerPlaylistsPane.removeAll();

        if (playpane != null) {
            centerPlaylistsPane.add(new JLabel(playpane.getName() + "`s playlist:") {{
                setBorder(new EmptyBorder(6, 6, 3, 0));
                setFont(Registry.headersFontSmall);
                setForeground(Color.WHITE);
            }}, BorderLayout.NORTH);
            centerPlaylistsPane.add(playpane, BorderLayout.CENTER);
        }

        playListsScroll.repaint();
        playListsScroll.revalidate();
    }

    public static void enableControls(boolean enable) {
        rightInfoPane.setVisible(enable && isInfoShowed);
        toolBar.setVisible(enable);
        bindListBtn.setEnabled(enable);
        clearBindBtn.setEnabled(enable);
        if (!enable) {
            centerPlaylistsPane.removeAll();
            centerPlaylistsPane.repaint();
        }
        updateGUI();
    }

    private static void updateGUI() {
        SwingUtilities.invokeLater(() -> {
            if (downShedulePane != null && DayCore.getDayItem(0) != null) {
                int height = toolBar.isShowing() ? maxDownPaneHeight : maxDownPaneHeight - 32;
                downShedulePane.setPreferredSize(new Dimension(
                        0,
                        frame.getHeight() / 2 > height ? height : frame.getHeight() / 2));
            }
            playProgress.setPreferredSize(new Dimension(frame.getWidth() / 3, 27));
            rightInfoPane.setPreferredSize(new Dimension(frame.getWidth() / 4, 0));
            frame.repaint();
        });
    }

    private static void loadDays() {
        playListsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        toolBar.setVisible(false);
        rightInfoPane.setVisible(false);

        playProgress.setString("Load media...");
        playProgress.setIndeterminate(true);

        daysCounter = 0;
        for (String day : DayCore.getDays()) {
            Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Try to add the day '" + day + "'...");

            try {
                playDatePane.add(DayCore.getDayItem(daysCounter));
            } catch (Exception e) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, "Is playDatePane`s add err: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showConfirmDialog(null,
                        "Не удалось добавить\nдень #" + daysCounter + ":\n" + e.getMessage(), "Ошибка!",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }

            daysCounter++;
        }

        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Loading tracks accomplished.");
        resetDownPaneSelect();

        toolBar.setVisible(true);

        playProgress.setString(null);
        playProgress.setIndeterminate(false);
        playListsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightInfoPane.setVisible(isInfoShowed);
    }

    private static void removeRow() {
        if (getSelectedItem().getPlayPane().getSelectedIndex() == -1) {
            return;
        }

        int req = JOptionPane.showConfirmDialog(null,
                "<html>Удалить трек #" + (getSelectedItem().getPlayPane().getSelectedIndex()+1) + "?<br>" +
                        "<b>" + (getSelectedItem().getPlayPane().getTrack(getSelectedItem().getPlayPane().getSelectedIndex()).getFileName()) + "</b>",
                "Уверен?", JOptionPane.WARNING_MESSAGE);

        if (req == 0) {
            getSelectedItem().removeSelected();
        }
    }

    // getters & setters:
    public static ArrayList<PlayDataItem> getWeekdayItems() {
        ArrayList<PlayDataItem> result = new ArrayList<>(7);

        for (Component comp : playDatePane.getComponents()) {
            if (comp instanceof PlayDataItem) {
                result.add((PlayDataItem) comp);
            }
        }

        return result;
    }

    public static PlayDataItem getSelectedItem() {
        for (PlayDataItem comp : getWeekdayItems()) {
            if (comp.isSelected()) {
                return comp;
            }
        }
        return null;
    }

    private static List<PlayDataItem> getSoundedItems() {
        List<PlayDataItem> result = new ArrayList<>();

        for (PlayDataItem comp : getWeekdayItems()) {
            if (comp.isPlayed()) {
                result.add(comp);
            }
        }

        return result;
    }

    public static void updateInfo(ListRow row) {
        selTrackName.setText("<html> <b color='#00FFFF'>Name:</b> " + row.getPath().toFile().getName().substring(0, row.getPath().toFile().getName().length() - 4));
        selTrackName.setToolTipText("" + row.getPath().toFile().getName().substring(0, row.getPath().toFile().getName().length() - 4));
        selTrackPath.setText("<html> <b color='#00FFFF'>Path:</b> " + row.getPath());
        selTrackPath.setToolTipText("" + row.getPath());
        try {
            selTrackDuration.setText("<html> <b color='#00FFFF'>Duration:</b> " + row.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
        selTrackSize.setText("<html> <b color='#00FFFF'>Size:</b> " + String.format("%.2f", row.getPath().toFile().length() / 1000f / 1000f) + " mb.");
    }

    public static void setPlayedLabelText(String mes) {
        SwingUtilities.invokeLater(() -> {
            nowPlayedLabel.setText(mes);
            centerPlaylistsPane.repaint();
        });
    }

    public static void updatePlayedLabelText() {
        SwingUtilities.invokeLater(() -> {
            List<PlayDataItem> played = getSoundedItems();
            String mes = "<html>Playing: ";
            for (PlayDataItem playItem : played) {
                mes += "<b color='YELLOW'>" + playItem.getName() + ":</b> '" + playItem.getActiveTrackName() + "' ";
            }

            nowPlayedLabel.setText(mes);
            try {
//                setProgress(100 / getSelectedItem().getPlayPane().getRowsCount() * (getSelectedItem().getIndexOfPlayed() + 1));
                updateProgress();
            } catch (Exception e) {/* IGNORE 'value / 0' */}
            centerPlaylistsPane.repaint();
        });
    }

    public static void setCurrentTimeText(String s) {
        currentTime.setText(s);
    }

    public static JFrame getFrame() {
        return frame;
    }

    private static void updateProgress() {
        playProgress.setString((getSelectedItem().getPlayPane().getPlayedIndex() + 1) + " из " + getSelectedItem().getPlayPane().getRowsCount());
    }

    private void tray() throws AWTException {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Tray the frame...");

        frame.dispose();
        tray.add(trayIcon);
        trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
    }

    private void detray() {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "De-Tray the frame...");
        BackVocalFrame.this.setVisible(true);
        BackVocalFrame.this.setState(JFrame.NORMAL);
        tray.remove(trayIcon);
    }

    private void exit() {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Try to stop the executors...");
        DayCore.shutdown();

        // saving days:
        for (PlayDataItem wdItem : getWeekdayItems()) {
            wdItem.saveToFile();
        }

        Out.Print(PlayDataItem.class, Out.LEVEL.INFO, "Finish at " + DayCore.getFormatted(System.currentTimeMillis() - MainClass.getStartTime()));
        BackVocalFrame.this.dispose();

        if (DayCore.isShutdowned()) {
            Out.close();
            MainClass.exit(Codes.OLL_OK);
        } else {
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "DayCore can`t close the executors correctly! ");
            Out.close();
            MainClass.exit(Codes.NOT_CORRECT_SHUTDOWN);
        }
    }


    // Listeners:
    @Override
    public void windowOpened(WindowEvent e) {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "The frame is open now.");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this,
                "Ты уверен?", "Завершить работу приложения?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
        if (req == 0) {
            exit();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Frame is closed now.");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();

            PopupMenu popup = new PopupMenu() {
                {
                    MenuItem defaultItem = new MenuItem("Show the frame");
                    defaultItem.addActionListener(e13 -> detray());

                    MenuItem close = new MenuItem("Exit");
                    close.addActionListener(e12 -> exit());

                    add(defaultItem);
                    add(close);
                }
            };

            trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("./resources/icons/0.png"), "BVF", popup);
            trayIcon.addActionListener(e1 -> detray());
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("BackVocalStudio");

            try {
                tray();
            } catch (AWTException awtException) {
                awtException.printStackTrace();
                Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Tray exception: " + awtException.getMessage());
            }
        }
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        detray();
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {updateGUI();}
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        int req;

        switch (e.getActionCommand()) {
            case "fill":
                req = JOptionPane.showConfirmDialog(null,
                        "Перестроить лист?", "Уверен?", JOptionPane.WARNING_MESSAGE);
                if (req == 0) {
                    fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fch.setMultiSelectionEnabled(false);
                    fch.setDialogTitle("Папка с треками:");
                    FileFilter filter = new FileNameExtensionFilter("MP3 File","mp3");
                    fch.setFileFilter(filter);

                    int result = fch.showOpenDialog(BackVocalFrame.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        getSelectedItem().getPlayPane().clearTracks();
                        getSelectedItem().getPlayPane().setTracks(fch.getSelectedFile());
                    }
                }
                break;

            case "reset":
                req = JOptionPane.showConfirmDialog(BackVocalFrame.this,
                        "Очистить выбранный плейлист?", "Подтверждение:", JOptionPane.OK_OPTION);
                if (req == 0) {
                    Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Clearing the playlist " + getSelectedItem().getName());
                    getSelectedItem().stop();
                    getSelectedItem().getPlayPane().clearTracks();
                    getSelectedItem().repaint();
                }
                break;
            default:
        }
    }

    // subclasses
    private static class DownSchedulePanel extends JPanel {
        public DownSchedulePanel() {
            setLayout(new BorderLayout(0, 0));

            setBackground(Color.BLACK);

            toolBar = new JToolBar("Можно тягать!") {
                {
                    setBorder(new EmptyBorder(0, 0, 1, 0));

                    moveUpBtn = new JButton("Поднять") {
                        {
                            setBackground(Color.RED);
                            setForeground(Color.BLUE);
                            setFont(Registry.btnsFont2);
                            addActionListener(e -> getSelectedItem().moveSelectedUp());
                        }
                    };

                    addTrackBtn = new JButton("+ трек") {
                        {
                            setForeground(Color.GREEN.darker());
                            setFont(Registry.btnsFont2);
                            addActionListener(e -> {
                                fch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                fch.setMultiSelectionEnabled(true);
                                fch.setDialogTitle("Выбор треков:");
                                FileFilter filter = new FileNameExtensionFilter("MP3 File","mp3");
                                fch.setFileFilter(filter);

                                int result = fch.showOpenDialog(DownSchedulePanel.this);
                                if (result == JFileChooser.APPROVE_OPTION) {
                                    ArrayList<Path> res = new ArrayList<>();
                                    for (File selectedFile : fch.getSelectedFiles()) {
                                        res.add(selectedFile.toPath());
                                    }
                                    getSelectedItem().getPlayPane().setTracks(res);
                                }
                            });
                        }
                    };

                    removeBtn = new JButton("- трек") {
                        {
                            setForeground(Color.RED);
                            setFont(Registry.btnsFont2);
                            addActionListener(e -> {
                                removeRow();
                            });
                        }
                    };

                    moveDownBtn = new JButton("Опустить") {
                        {
                            setForeground(Color.BLUE);
                            setFont(Registry.btnsFont2);
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    getSelectedItem().moveSelectedDown();
                                }
                            });
                        }
                    };

                    showInfoBtn = new JButton("Инфо") {
                        {
                            setForeground(Color.GRAY);
                            setFont(Registry.btnsFont2);
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    isInfoShowed = !isInfoShowed;
                                    rightInfoPane.setVisible(isInfoShowed);
                                    frame.repaint();
                                }
                            });
                        }
                    };

                    add(moveUpBtn);
                    add(new JLabel(" | "));
                    add(addTrackBtn);
                    add(removeBtn);
                    add(new JLabel(" | "));
                    add(moveDownBtn);
                    add(new JLabel(" | "));
                    add(showInfoBtn);
                }
            };

            playDatePane = new JPanel(new GridLayout(1, 7, 1, 0)) {
                {
                    setBackground(Color.BLACK);
                    setBorder(null);
                }
            };

            playDateScroll = new JScrollPane(playDatePane) {
                {
                    setBorder(null);
                    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    getVerticalScrollBar().setUnitIncrement(16);
                }
            };

            JPanel downPane = new JPanel(new BorderLayout()) {
                {
                    setOpaque(false);
                    setBorder(new EmptyBorder(1, 0, 0, 0));

                    downBtnsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                        {
                            setBackground(Color.DARK_GRAY);
                            setBorder(new EmptyBorder(0, 0, 1, 0));

                            bindListBtn = new JButton("Залить из папки") {
                                {
                                    setFont(Registry.btnsFont);
                                    setEnabled(false);
                                    setFocusPainted(false);
                                    setBackground(new Color(0.3f, 0.5f, 0.2f, 1.0f));
                                    setForeground(Color.BLACK);
                                    setActionCommand("fill");
                                    addActionListener(alist);
                                }
                            };

                            clearBindBtn = new JButton("Сброс листа") {
                                {
                                    setFont(Registry.btnsFont);
                                    setEnabled(false);
                                    setFocusPainted(false);
                                    setBackground(new Color(0.5f, 0.2f, 0.2f, 1.0f));
                                    setForeground(Color.BLACK);
                                    setActionCommand("reset");
                                    addActionListener(alist);
                                }
                            };

                            playProgress = new JProgressBar(0, 0, 100) {
                                {
                                    setFont(Registry.btnsFont);
                                    setStringPainted(true);
                                }
                            };

                            nowPlayedLabel = new JLabel() {
                                {
                                    setFont(Registry.headersFontSmall);
                                    setForeground(Color.WHITE);
                                }
                            };

                            add(bindListBtn);
                            add(clearBindBtn);
                            add(new JSeparator(1));
                            add(playProgress);
                            add(new JSeparator(1));
                            add(nowPlayedLabel);
                        }
                    };

                    add(downBtnsPane);
                }
            };

            add(toolBar, BorderLayout.NORTH);
            add(playDateScroll, BorderLayout.CENTER);
            add(downPane, BorderLayout.SOUTH);
        }
    }

    private static class RightInfoPanel extends JPanel {
        public RightInfoPanel() {
            setLayout(new GridLayout(18, 1, 0, 0));
            setBackground(Color.BLACK);
            setBorder(new EmptyBorder(3, 3, 0, 0));
            setVisible(false);

            add(new JLabel("<Track info>") {{
                setForeground(Color.WHITE);
                setFont(Registry.infoFont0);
                setHorizontalAlignment(JLabel.CENTER);
            }});
            currentTime = new JLabel(DayCore.getFormatted(System.currentTimeMillis())) {{
                setForeground(Color.WHITE);
                setFont(Registry.infoFont0);
                setHorizontalAlignment(JLabel.LEFT);
            }};
            add(currentTime);
            add(new JSeparator());

            selTrackName = new RightLabel();
            selTrackPath = new RightLabel();
            selTrackDuration = new RightLabel();
            selTrackSize = new RightLabel();

            add(selTrackName);
            add(selTrackPath);
            add(selTrackDuration);
            add(selTrackSize);
        }
    }

    public static class RightLabel extends JLabel {
        public RightLabel() {
            setForeground(Color.WHITE);
            setFont(Registry.infoFont0);
            setHorizontalAlignment(JLabel.LEFT);
        }
    }
}
