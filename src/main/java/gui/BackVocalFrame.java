package gui;

import door.DayCore;
import door.ErrorSender;
import door.Exit;
import door.MainClass;
import fox.components.ListRow;
import fox.components.PlayPane;
import fox.fb.FoxFontBuilder;
import fox.ia.InputAction;
import fox.out.Out;
import fox.render.FoxRender;
import fox.utils.FOptionPane;
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
    private static JPanel basePane, northLabelsPane, centerPlayPane, playDatePane, downBtnsPane, downShedulePane, rightInfoPane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn, moveUpBtn, moveDownBtn, removeBtn, addTrackBtn, showInfoBtn, saveBtn, loadBtn;
    private static JLabel weekdayLabel, nowPlayedLabel, currentTime, selTrackName, selTrackPath, selTrackDuration, selTrackSize;
    private static JProgressBar playProgress;
    private static JFileChooser fch = new JFileChooser("./resources/audio/");
    private static JToolBar toolBar;
    private static JMenuBar menuBar = new JMenuBar();
    private static JRadioButtonMenuItem one, two;

    private static int daysCounter = 0, maxDownPaneHeight = 220;
    private static boolean isInfoShowed;
    private static ActionListener alist;

    private static String alSt1 = "*** Воспроизведение оповещения ***";
    private static String alSt2 = "(жми стоп для прерывания)";
    private static float alF1, alF2;
    private static BufferedImage alarmInfo;
    private static BufferedImage im, pcIco, mdIco, indON, indOff, indOrange;

    private static JButton b1, b2, b3;
    private static BufferedImage i1, i2, i3;


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getSelectedItem() == null) {return;}
        if (getSelectedItem().isAlarmPlayed()) {
            g.drawImage(alarmInfo, getWidth() / 2 - alarmInfo.getWidth() / 2, getHeight() / 3, BackVocalFrame.this);
        }
    }

    private void rebuildAlIn() {
        try {
            im = ImageIO.read(new File("./resources/icons/help.png"));
            indON = ImageIO.read(new File("./resources/icons/iGreen.png"));
            indOff = ImageIO.read(new File("./resources/icons/iOff.png"));
            indOrange = ImageIO.read(new File("./resources/icons/iOrange.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        g.setColor(Color.ORANGE);
        g.fillRoundRect(3,3, alarmInfo.getWidth() - 4, alarmInfo.getHeight() - 4,14, 14);

        g.setColor(Color.DARK_GRAY);
        g.drawRoundRect(5,5, alarmInfo.getWidth() - 9, alarmInfo.getHeight() - 9,12, 12);

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
        try {
            ico = new ImageIcon(ImageIO.read(new File("./resources/icons/0.png")));
            setIconImage(ico.getImage());
        } catch (Exception e) {
            Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Ico image can`t load." + e.getMessage());
            e.printStackTrace();
        }
        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        basePane = new JPanel(new BorderLayout(1, 3)) {
            {
                setBackground(Color.BLACK);

                centerPlayPane = new JPanel(new BorderLayout(3, 3)) {
                    {setOpaque(false);}
                };
                playListsScroll = new JScrollPane(centerPlayPane) {
                    {
                        setBorder(null);
                        setFont(Registry.trackSelectedFont);
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

                northLabelsPane = new JPanel(new BorderLayout(3,3)) {
                    {
                        setOpaque(false);

                        weekdayLabel = new JLabel() {
                            {
                                setBorder(new EmptyBorder(6, 6, 3, 0));
                                setFont(Registry.headersFontSmall);
                                setForeground(Color.WHITE);
                            }
                        };

                        JPanel indicatorsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                            {
                                setOpaque(false);

                                b1 = new JButton() {
                                    @Override
                                    public void paint(Graphics g) {
                                        i1 = getSelectedItem().isHandStopped() ? indOrange : indOff;
                                        g = FoxRender.setMedRender((Graphics2D) g);
                                        g.drawImage(i1, 0, 0, getWidth(), getHeight(), null);
                                    }

                                    {
                                        setBorder(null);
                                        setFocusable(false);
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(24, 24));
                                    }
                                };
                                b2 = new JButton() {
                                    @Override
                                    public void paint(Graphics g) {
                                        i2 = getSelectedItem().isAlarmPlayed() ? indON : indOff;
                                        g = FoxRender.setMedRender((Graphics2D) g);
                                        g.drawImage(i2, 0, 0, getWidth(), getHeight(), null);
                                    }

                                    {
                                        setBorder(null);
                                        setFocusable(false);
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(24, 24));
                                    }
                                };
                                b3 = new JButton() {
                                    @Override
                                    public void paint(Graphics g) {
                                        i3 = getSelectedItem().isPlayed() ? indON : indOff;
                                        g = FoxRender.setMedRender((Graphics2D) g);
                                        g.drawImage(i3, 0, 0, getWidth(), getHeight(), this);
                                        g.dispose();
                                    }

                                    {
                                        setBorder(null);
                                        setFocusable(false);
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(24, 24));
                                    }
                                };

                                add(new JLabel("Пауза:") {{setBorder(new EmptyBorder(6, 6, 3, 0));
                                    setFont(Registry.headersFontSmall);
                                    setForeground(Color.WHITE);}});
                                add(b1);
                                add(new JLabel("Оповещение:") {{setBorder(new EmptyBorder(6, 6, 3, 0));
                                    setFont(Registry.headersFontSmall);
                                    setForeground(Color.WHITE);}});
                                add(b2);
                                add(new JLabel("Играет:") {{setBorder(new EmptyBorder(6, 6, 3, 0));
                                    setFont(Registry.headersFontSmall);
                                    setForeground(Color.WHITE);}});
                                add(b3);
                            }
                        };

                        add(weekdayLabel, BorderLayout.WEST);
                        add(indicatorsPane, BorderLayout.EAST);
                    }
                };

                add(northLabelsPane, BorderLayout.NORTH);
                add(playListsScroll, BorderLayout.CENTER);
                add(rightInfoPane, BorderLayout.EAST);
                add(downShedulePane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        try {buildMenuBar();
        } catch (IOException e) {
            e.printStackTrace();
            Out.Print(MainClass.class, Out.LEVEL.WARN, "Has a some problem with a loading menu bar: " + e.getMessage());
        }

        addWindowListener(this);
        addComponentListener(this);

        loadDays();

        InputAction.add("frame", frame);
        InputAction.set("frame", "deleteRow", KeyEvent.VK_DELETE, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getID() == 1001) {
                    removeRow();
                }
            }
        });

        MainClass.closeLogo();
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Show the frame...");
        checkMode();

        selectCurrentDay();
        DayCore.execute();

        rebuildAlIn();
    }

    private void checkMode() {
        System.out.println("checkMode()...");
        setVisible(true);

        Component ei = DayCore.getDayItem(0);
        while (ei.getWidth() <= 10 && ei.isVisible()) {
            if (ei.getWidth() == 0) {Thread.currentThread().yield();}
        }

        setMinimumSize(new Dimension(ei.getWidth() * 5 + 18, 600));

        if (one.isSelected()) {
            setPreferredSize(new Dimension(ei.getWidth() * 7 + 24, Toolkit.getDefaultToolkit().getScreenSize().height));
            playDatePane.setPreferredSize(null);
        } else {
            setPreferredSize(new Dimension(ei.getWidth() * 6 + 21, Toolkit.getDefaultToolkit().getScreenSize().height));
            playDatePane.setPreferredSize(new Dimension(1024, 0));
        }

        pack();
        setLocationRelativeTo(null);
    }

    private void buildMenuBar() throws IOException {
        try {
            pcIco = ImageIO.read(new File("./resources/icons/pc.png"));
            mdIco = ImageIO.read(new File("./resources/icons/md.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JMenu file = new JMenu("Файл") {
            {
                JMenuItem exit = new JMenuItem("Выход") {
                    {
                        setIcon(new ImageIcon("images/exit.png"));
                        addActionListener(e -> exit());
                    }
                };

                add(exit);
            }
        };

        JMenu view = new JMenu("Вид") {
            {
//                JCheckBoxMenuItem grid  = new JCheckBoxMenuItem("angelicalis39@mail.ru");

                ButtonGroup bg = new ButtonGroup();
                one = new JRadioButtonMenuItem("Режим PC", pcIco == null ? null : new ImageIcon(pcIco), true) {
                    {
                        addActionListener(e -> checkMode());
                    }
                };
                two = new JRadioButtonMenuItem("Режим MD", mdIco == null ? null : new ImageIcon(mdIco), false) {
                    {
                        addActionListener(e -> checkMode());
                    }
                };
                bg.add(one);
                bg.add(two);

                add(new JLabel("angelicalis39@mail.ru") {{setBorder(new EmptyBorder(3,12,6,0));}});
                add(new JSeparator());
                add(one);
                add(two);
            }
        };

        menuBar.add(file);
        menuBar.add(view);

        setJMenuBar(menuBar);
    }

    private void selectCurrentDay() {
        for (PlayDataItem weekdayItem : getWeekdayItems()) {
            if (weekdayItem.isToday()) {
                weekdayItem.setSelected(true);
            }
        }
    }

    public static void resetDownPaneSelect() {
        for (PlayDataItem comp : getWeekdayItems()) {
            if (comp.isSelected()) {comp.setSelected(false);}
        }
    }

    public static void showPlayList(PlayPane playpane) {
        System.out.println("showPlayList()...");
        centerPlayPane.removeAll();

        if (playpane != null) {
            weekdayLabel.setText(playpane.getName() + "`s playlist:");
            centerPlayPane.add(playpane, BorderLayout.CENTER);
            northLabelsPane.setVisible(true);
        }

        playListsScroll.repaint();
        playListsScroll.revalidate();
    }

    public static void enableControls(boolean enable) {
        System.out.println("enableControls()...");

        rightInfoPane.setVisible(enable && isInfoShowed);
        toolBar.setVisible(enable);
        bindListBtn.setEnabled(enable);
        clearBindBtn.setEnabled(enable);
        northLabelsPane.setVisible(true);
        if (!enable) {
            northLabelsPane.setVisible(false);
            centerPlayPane.removeAll();
            centerPlayPane.repaint();
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
        for (String dayname : DayCore.getDays()) {
            Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Try to add the day '" + dayname + "'...");

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

        int req = new FOptionPane(
                "Ты уверен?",
                "Удалить треки\nв количестве " + getSelectedItem().getSelectedIndexes().length + " штук?",
                FOptionPane.TYPE.YES_NO_TYPE, im)
                .get();

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
            centerPlayPane.repaint();
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
            centerPlayPane.repaint();
            northLabelsPane.repaint();
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

    private void tray() {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Tray the frame...");

        try {
            tray.add(trayIcon);
            frame.dispose();
            trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Tray exception: " + e.getMessage());
            e.printStackTrace();
        }
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
            Exit.exit(Codes.OLL_OK.code(), null);
        } else {
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "DayCore can`t close the executors correctly! ");
            Out.close();
            Exit.exit(Codes.NOT_CORRECT_SHUTDOWN.code(), "DayCore can`t close the executors correctly!");
        }
    }


    // Listeners:
    @Override
    public void windowOpened(WindowEvent e) {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "The frame is open now.");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = new FOptionPane(
                "Ты уверен?",
                "Завершить работу приложения?",
                FOptionPane.TYPE.YES_NO_TYPE, im)
                .get();

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

            tray();
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
                req = new FOptionPane(
                        "Ты уверен?",
                        "Перестроить лист?",
                        FOptionPane.TYPE.YES_NO_TYPE, im)
                        .get();
                if (req == 0) {
                    fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fch.setMultiSelectionEnabled(false);
                    fch.setDialogTitle("Папка с треками:");
                    FileFilter filter = new FileNameExtensionFilter("MP3 File","mp3");
                    fch.setFileFilter(filter);

                    int result = fch.showOpenDialog(BackVocalFrame.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        System.out.println("getActionCommand()");
                        getSelectedItem().getPlayPane().clearTracks();
                        getSelectedItem().getPlayPane().setTracks(fch.getSelectedFile());
                    }
                }
                break;

            case "reset":
                req = new FOptionPane(
                        "Подтверждение:",
                        "Очистить выбранный плейлист?",
                        FOptionPane.TYPE.YES_NO_TYPE, im)
                        .get();
                if (req == 0) {
                    System.out.println("getActionCommand()");
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

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
        }


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
                            addActionListener(e -> removeRow());
                        }
                    };

                    moveDownBtn = new JButton("Опустить") {
                        {
                            setForeground(Color.BLUE);
                            setFont(Registry.btnsFont2);
                            addActionListener(e -> getSelectedItem().moveSelectedDown());
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

                    saveBtn = new JButton("Сохранить день") {
                        {
                            setFont(Registry.btnsFont3);
                            addActionListener(e -> {
                                getSelectedItem().saveToFile();
                                new FOptionPane("Сохранено!", "Плейлист сохранен.", null, null);
                            });
                        }
                    };
                    loadBtn = new JButton("Загрузить день") {
                        {
                            setFont(Registry.btnsFont3);
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
//                                    PlayDataItem di = DayCore.getDayItem(getSelectedItem().getIndex());
//                                    playDatePane.remove(di);
//                                    DayCore.loadDay(di.getName(), di.getIndex());
//                                    playDatePane.add(di, di.getIndex());
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
                    add(new JLabel(" |      "));
                    add(saveBtn);
//                    add(loadBtn);
                }
            };

            playDatePane = new JPanel(new GridLayout(1, 7, 0, 0)) {
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
                    getHorizontalScrollBar().setUnitIncrement(96);
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
            setLayout(new GridLayout(16, 1, 0, 0));
            setBackground(Color.BLACK);
            setBorder(new EmptyBorder(3, 3, 0, 0));
            setVisible(false);

            add(new JLabel("<Информация>") {{
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
