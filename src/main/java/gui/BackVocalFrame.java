package gui;

import door.MainClassMy;
import fox.components.AlarmItem;
import fox.components.ListRow;
import fox.components.PlayPane;
import fox.fb.FoxFontBuilder;
import fox.out.Out;
import registry.CodesMy;
import registry.RegistryMy;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class BackVocalFrame extends JFrame implements WindowListener, ComponentListener {
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static ExecutorService executor;

    private static BackVocalFrame frame;
    private static JPanel basePane, centerPlaylistsPane, playDatePane, downBtnsPane, downShedulePane, rightInfoPane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn, moveUpBtn, moveDownBtn, removeBtn, addTrackBtn, showInfoBtn;
    private static JLabel nowPlayedLabel, currentTime, selTrackName, selTrackPath, selTrackDuration, selTrackSize;
    private static JProgressBar playProgress;
    private static JFileChooser fch = new JFileChooser("./resources/audio/");
    private static JToolBar toolBar;

    private static final PlayDataItemMy[] dayItems = new PlayDataItemMy[7];
    private static final String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private static final Font headersFontSmall = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private static final Font btnsFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, false);
    private static final Font btnsFont2 = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private static final Font infoFont0 = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.CANDARA, 14, true);

    private static int daysCounter = 0, maxDownPaneHeight = 220;
    private final SimpleDateFormat weakday = new SimpleDateFormat("EEEE", Locale.US);
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static boolean isInfoShowed;


    public BackVocalFrame() {
        frame = this;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Build the frame...");

        try {
            setIconImage(new ImageIcon(ImageIO.read(new File("./resources/icons/0.png"))).getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Back vocal studio v." + RegistryMy.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        basePane = new JPanel(new BorderLayout(1, 3)) {
            {
                setBackground(Color.BLACK);

                centerPlaylistsPane = new JPanel(new BorderLayout(3, 3)) {
                    {
                        setOpaque(false);
                    }
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

                rightInfoPane = new JPanel(new GridLayout(18, 1, 0, 0)) {
                    {
                        setBackground(Color.BLACK);
                        setBorder(new EmptyBorder(3, 3, 0, 0));
                        setVisible(false);

                        add(new JLabel("<Track info>") {{
                            setForeground(Color.WHITE);
                            setFont(infoFont0);
                            setHorizontalAlignment(JLabel.CENTER);
                        }});
                        currentTime = new JLabel(sdf.format(System.currentTimeMillis())) {{
                            setForeground(Color.WHITE);
                            setFont(infoFont0);
                            setHorizontalAlignment(JLabel.LEFT);
                        }};
                        add(currentTime);
                        add(new JSeparator());

                        selTrackName = new JLabel() {{
                            setForeground(Color.WHITE);
                            setFont(infoFont0);
                            setHorizontalAlignment(JLabel.LEFT);
                        }};
                        selTrackPath = new JLabel() {{
                            setForeground(Color.WHITE);
                            setFont(infoFont0);
                            setHorizontalAlignment(JLabel.LEFT);
                        }};
                        selTrackDuration = new JLabel() {{
                            setForeground(Color.WHITE);
                            setFont(infoFont0);
                            setHorizontalAlignment(JLabel.LEFT);
                        }};
                        selTrackSize = new JLabel() {{
                            setForeground(Color.WHITE);
                            setFont(infoFont0);
                            setHorizontalAlignment(JLabel.LEFT);
                        }};

                        add(selTrackName);
                        add(selTrackPath);
                        add(selTrackDuration);
                        add(selTrackSize);
                    }
                };

                downShedulePane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setBackground(Color.BLACK);

                        toolBar = new JToolBar("Можно тягать!") {
                            {
                                setBorder(new EmptyBorder(0, 0, 1, 0));

                                moveUpBtn = new JButton("Move Up") {
                                    {
                                        setBackground(Color.RED);
                                        setForeground(Color.BLUE);
                                        setFont(btnsFont2);
                                        addActionListener(e -> getSelectedItem().moveSelectedUp());
                                    }
                                };

                                addTrackBtn = new JButton("+ трек") {
                                    {
                                        setForeground(Color.GREEN);
                                        setFont(btnsFont2);
                                        addActionListener(e -> {
                                            fch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                            fch.setMultiSelectionEnabled(true);
                                            fch.setDialogTitle("Выбор треков:");
                                            FileFilter filter = new FileNameExtensionFilter("MP3 File","mp3");
                                            fch.setFileFilter(filter);

                                            int result = fch.showOpenDialog(BackVocalFrame.this);
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
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                if (getSelectedItem().getPlayPane().getSelectedIndex() == -1) {
                                                    return;
                                                }

                                                int req = JOptionPane.showConfirmDialog(null,
                                                        "Delete track #" + (getSelectedItem().getPlayPane().getSelectedIndex() + 1) + "?",
                                                        "Sure?", JOptionPane.WARNING_MESSAGE);

                                                if (req == 0) {
                                                    getSelectedItem().removeSelected();
                                                }
                                            }
                                        });
                                    }
                                };

                                moveDownBtn = new JButton("Move Down") {
                                    {
                                        setForeground(Color.BLUE);
                                        setFont(btnsFont2);
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
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                isInfoShowed = !isInfoShowed;
                                                rightInfoPane.setVisible(isInfoShowed);
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
                                                setFont(btnsFont);
                                                setEnabled(false);
                                                setFocusPainted(false);
                                                setBackground(new Color(0.3f, 0.5f, 0.2f, 1.0f));
                                                setForeground(Color.BLACK);
                                                addActionListener(e -> {
                                                    int req = JOptionPane.showConfirmDialog(null,
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
                                                });
                                            }
                                        };

                                        clearBindBtn = new JButton("Сброс листа") {
                                            {
                                                setFont(btnsFont);
                                                setEnabled(false);
                                                setFocusPainted(false);
                                                setBackground(new Color(0.5f, 0.2f, 0.2f, 1.0f));
                                                setForeground(Color.BLACK);
                                                addActionListener(new ActionListener() {
                                                    @Override
                                                    public void actionPerformed(ActionEvent e) {
                                                        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this,
                                                                "Очистить выбранный плейлист?", "Подтверждение:", JOptionPane.OK_OPTION);
                                                        if (req == 0) {
                                                            Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Clearing the playlist " + getSelectedItem().getName());
                                                            getSelectedItem().getPlayPane().clearTracks();
                                                        }
                                                    }
                                                });
                                            }
                                        };

                                        playProgress = new JProgressBar(0, 0, 100) {
                                            {
                                                setFont(btnsFont);
                                                setStringPainted(true);
                                            }
                                        };

                                        nowPlayedLabel = new JLabel() {
                                            {
                                                setFont(headersFontSmall);
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
                };

                add(playListsScroll, BorderLayout.CENTER);
                add(rightInfoPane, BorderLayout.EAST);
                add(downShedulePane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        addWindowListener(this);
        addComponentListener(this);

        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Show the frame...");
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        loadDays();

        setMinimumSize(new Dimension(dayItems[0].getWidth() * 7 + 6, 600));
        setLocationRelativeTo(null);
        repaint();

        runExecutors();
    }

    private void runExecutors() {
        try {
            String today = weakday.format(new Date());
            Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Starting the Executors...");

            executor = Executors.newFixedThreadPool(2);
            executor.execute(() -> {
                try {
                    for (PlayDataItemMy weakdayItem : getWeekdayItems()) {
                        if (!weakdayItem.getName().equalsIgnoreCase(today)) {
                            continue;
                        }

                        if (!weakdayItem.inSchedulingTimeAccept()) {
                            if (weakdayItem.isPlayed()) {
                                weakdayItem.stop();
                                JOptionPane.showConfirmDialog(BackVocalFrame.this, "Timer out! Music has stopped.", "Timer out:", JOptionPane.DEFAULT_OPTION);
                            }
                        } else {
                            if (weakdayItem.getPlayPane().isEmpty()) {
                                continue;
                            }

                            if (!weakdayItem.isPlayed() && !weakdayItem.isPaused() && !weakdayItem.isHandStopped()) {
                                weakdayItem.play();
                                weakdayItem.setSelected(true);
                            }
                        }

                    }
                } catch (Exception e) {
                    Out.Print(getClass(), Out.LEVEL.WARN, "Exception into play executor: " + e.getMessage());
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Play executor was interrupted incorrectly.");
                    Thread.currentThread().interrupt();
                }
            });
            executor.execute(() -> {
                Out.Print(BackVocalFrame.class, Out.LEVEL.INFO, "== Launch time is: <" + sdf.format(System.currentTimeMillis() - MainClassMy.getStartTime()) + "> ==");

                try {
                    for (PlayDataItemMy weakdayItem : getWeekdayItems()) {
                        if (!weakdayItem.getName().equalsIgnoreCase(today)) {
                            continue;
                        }

                        String time;
                        ArrayList<AlarmItem> ail = weakdayItem.getAlarmData();
                        for (AlarmItem s : ail) {
                            if (s.isWasPlayed()) {
                                continue;
                            }

                            time = s.getTime();
                            if (weakdayItem.isTimeCome(time)) {
                                weakdayItem.pause();
                                weakdayItem.playAlarm(s.getTrack());
                                s.wasPlayed(true);
                                while (weakdayItem.alarmThreadIsAlive()) {
                                    Thread.yield();
                                }
                                weakdayItem.resume();
                            }
                        }

                    }

                } catch (Exception e) {
                    Out.Print(getClass(), Out.LEVEL.WARN, "Exception into alarms executor: " + e.getMessage());
                    e.printStackTrace();
                }

                currentTime.setText("<html>Now: <b color='YELLOW'>" + sdf.format(System.currentTimeMillis()) + "</b></html>");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Alarms executor was interrupted incorrectly.");
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            Out.Print(getClass(), Out.LEVEL.WARN, "Executors loading exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void resetDownPaneSelect() {
        for (PlayDataItemMy comp : getWeekdayItems()) {
            comp.setSelected(false);
        }
    }

    public static void showPlayList(PlayPane playpane) {
        centerPlaylistsPane.removeAll();

        if (playpane != null) {
            centerPlaylistsPane.add(new JLabel(playpane.getName() + "`s playlist:") {{
                setBorder(new EmptyBorder(6, 6, 3, 0));
                setFont(headersFontSmall);
                setForeground(Color.WHITE);
            }}, BorderLayout.NORTH);
            centerPlaylistsPane.add(playpane, BorderLayout.CENTER);
//            Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "The playlist named '" + playpane.getName() + "' was added to CENTER.");
        }

        playListsScroll.repaint();
        playListsScroll.revalidate();
    }

    public static ArrayList<PlayDataItemMy> getWeekdayItems() {
        ArrayList<PlayDataItemMy> result = new ArrayList<>(7);

        for (Component comp : playDatePane.getComponents()) {
            if (comp instanceof PlayDataItemMy) {
                result.add((PlayDataItemMy) comp);
            }
        }

        return result;
    }

    public static void updatePlayedLabelText() {
        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {/* IGNORE */}

            List<PlayDataItemMy> played = getSoundedItems();
            String mes = "<html>Playing: ";
            for (PlayDataItemMy playItem : played) {
                mes += "<b color='YELLOW'>" + playItem.getName() + ":</b> '" + playItem.getActiveTrackName() + "' ";
            }

            nowPlayedLabel.setText(mes);
            try {
                setProgress(100 / getSelectedItem().getPlayPane().getRowsCount() * (getSelectedItem().getIndexOfPlayed() + 1));
            } catch (Exception e) {/* IGNORE 'value / 0' */}
            centerPlaylistsPane.repaint();
        }).start();
    }

    public static void setPlayedLabelText(String mes) {
        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {/* IGNORE */}

            nowPlayedLabel.setText(mes);
            centerPlaylistsPane.repaint();
        }).start();
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static PlayDataItemMy getSelectedItem() {
        for (PlayDataItemMy comp : getWeekdayItems()) {
            if (comp.isSelected()) {
                return comp;
            }
        }
        return null;
    }

    private static List<PlayDataItemMy> getSoundedItems() {
        List<PlayDataItemMy> result = new ArrayList<>();

        for (PlayDataItemMy comp : getWeekdayItems()) {
            if (comp.isPlayed()) {
                result.add(comp);
            }
        }

        return result;
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

    private void stateChanged() {
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
    }

    private static void loadDays() {
        playListsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        toolBar.setVisible(false);
        rightInfoPane.setVisible(false);

        playProgress.setString("Load media...");
        playProgress.setIndeterminate(true);

        for (String day : days) {
            Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "Try to load the day '" + day + "'...");

            try {
                // META loading:
                String meta = Files.readString(Paths.get("./resources/scheduler/" + day + ".meta"), StandardCharsets.UTF_8);
                String[] data = meta.split("NN_");

                dayItems[daysCounter] = new PlayDataItemMy(
                        day,
                        data[1].split("_EE")[1],
                        data[2].split("_EE")[1],
                        Boolean.parseBoolean(data[3].split("_EE")[1]));


                // ALARMS loading:
                List<String> alarms = Files.lines(Paths.get("./resources/scheduler/" + day + ".alarms"), StandardCharsets.UTF_8).collect(Collectors.toList());
                for (String alarm : alarms) {
                    try {
                        String time = alarm.split(">")[0];
                        Path track = Paths.get(alarm.split(">")[1]);

                        if (Files.notExists(track)) {
                            Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Alarm file not exist:");
                        } else {
                            if (time.length() == 8) {
                                dayItems[daysCounter].addAlarm(time, track);
                            } else {
                                Out.Print(BackVocalFrame.class, Out.LEVEL.WARN, "Time is not correct: " + time);
                            }
                        }
                    } catch (Exception e) {
                        Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, "Alarms loading exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }


                // LIST loading:
                List<String> trackss = Files.lines(Paths.get("./resources/scheduler/" + day + ".list"), StandardCharsets.UTF_8).collect(Collectors.toList());
                List<Path> tracks = new ArrayList<>();
                for (String s : trackss) {
                    tracks.add(Paths.get(s));
                }
                dayItems[daysCounter].addTracks(tracks);
            } catch (IllegalArgumentException iae) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, iae.getMessage());
                iae.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException aibe) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, aibe.getMessage());
                aibe.printStackTrace();
            } catch (MalformedInputException mie) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, mie.getMessage());
                mie.printStackTrace();
            } catch (NoSuchFileException fnf) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, "PlayList for " + day + " is not exist.");
                dayItems[daysCounter] =
                        new PlayDataItemMy(
                                day,
                                "12:00:00", "12:00:00",
                                true);
            } catch (Exception e) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, "Meta loading err: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                playDatePane.add(dayItems[daysCounter]);
            } catch (Exception e) {
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, "Is playDatePane`s add err: " + e.getMessage());
                e.printStackTrace();
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


    private static void setProgress(int prog) {
        if (prog < 0) {
            prog = 0;
        } else if (prog > 100) {
            prog = 100;
        }
        playProgress.setValue(prog);
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
        executor.shutdown(); //shutdown executor

        try {
            int maxCycleStopAwait = 3;
            while (!executor.awaitTermination(3, TimeUnit.SECONDS) && maxCycleStopAwait > 0) {
                maxCycleStopAwait--;
                Out.Print(PlayDataItemMy.class, Out.LEVEL.WARN, "Waiting for a stop takes more time than seems...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!executor.isTerminated()) {
            Out.Print(PlayDataItemMy.class, Out.LEVEL.ERROR, "Executors can`t stopped, than was killed! It`s bad.");
            executor.shutdownNow();
        }

        // saving days:
        for (PlayDataItemMy wdItem : getWeekdayItems()) {
            wdItem.saveToFile();
        }

        Out.Print(PlayDataItemMy.class, Out.LEVEL.INFO, "Finish at " + sdf.format(System.currentTimeMillis() - MainClassMy.getStartTime()));
        BackVocalFrame.this.dispose();

        Out.close();
        MainClassMy.exit(CodesMy.OLL_OK);
    }


    // Listeners:
    @Override
    public void windowOpened(WindowEvent e) {
        Out.Print(BackVocalFrame.class, Out.LEVEL.DEBUG, "The frame is open now.");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this,
                "Are You sure?..", "Save and exit?",
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
    public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> {
            if (downShedulePane != null && dayItems[0] != null) {
                downShedulePane.setPreferredSize(new Dimension(0, frame.getHeight() / 2 > maxDownPaneHeight ? maxDownPaneHeight : frame.getHeight() / 2));
            }
            playProgress.setPreferredSize(new Dimension(frame.getWidth() / 3, 27));
            rightInfoPane.setPreferredSize(new Dimension(frame.getWidth() / 4, 0));
        });
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }
}
