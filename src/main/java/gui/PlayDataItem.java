package gui;

import fox.components.AlarmItem;
import fox.components.PlayPane;
import fox.ia.InputAction;
import fox.out.Out;
import fox.render.FoxRender;
import fox.utils.FOptionPane;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
import registry.Registry;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PlayDataItem extends JPanel implements MouseListener, ActionListener {
    private SimpleDateFormat weakday = new SimpleDateFormat("EEEE", Locale.US);

    private Player player;
    private PlayPane playpane;

    private JPanel dayControlPane;
    private JLabel dayNameLabel, inLabelH, inLabelM, inLabelS , outLabelH, outLabelM, outLabelS;
    private JCheckBox repeatCBox;
    private JButton startPlayBtn, nextPlayBtn, stopPlayBtn, alarmsBtn;
    private JFileChooser fch = new JFileChooser("./resources/audio/");

    private Color defBkgColor = Color.GRAY, secondColor, defTextColor = Color.BLACK, alarmsBack = Color.GRAY;
    private String timerIn = "00:00:00", timerOut = "23:59:59";

    private Thread musicThread, alarmThread;
    private boolean isSelected = false, repeat, isOver, isPlaying, isHandStopped = false, alarmIsPlayed = false;
    private int indexOfPlayed;
    private int downButtonsDim = 28;

    private DefaultListModel<AlarmItem> arm = new DefaultListModel();
    private JList<AlarmItem> alarmList;
    private LocalDateTime date;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = FoxRender.setMedRender((Graphics2D) g);

        // backbround:
        if (playpane == null || playpane.isEmpty()) {g2D.setColor(defBkgColor);
        } else {
            if (isOver) {
                defTextColor = Color.BLACK;
                g2D.setColor(Color.YELLOW);
            } else {
                if (getName().equals("Sunday") || getName().equals("Saturday")) {
                    defTextColor = Color.BLACK;
                    g2D.setColor(Color.CYAN.darker());
                } else {
                    defTextColor = Color.WHITE;
                    g2D.setColor(Color.DARK_GRAY);
                }
            }
        }
        g2D.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);

        if (isSelected) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(Color.GREEN);
            g2D.drawRoundRect(1,1,getWidth() - 4,getHeight() - 4,9,9);
        }

        Color justExistColor = new Color(1.0f, 0.8f, 0.25f, 0f);
        Color existAndPlayedColor = new Color(0.0f, 1.0f, 0.0f, 0f);
        Color alarmSoundedColor = new Color(1.0f, 0.25f, 0.0f, 0f);

        // oval:
        float opacity = 0.75f;
        if (playpane == null || playpane.isEmpty()) {
            g2D.setColor(new Color(0.35f, 0.35f, 0.35f, opacity));
        } else {
            opacity = 0f;
            Color drawColor = alarmIsPlayed ? alarmSoundedColor : isPlaying ? existAndPlayedColor : justExistColor;

            for (int i = 0; i < 10; i++) {
                opacity += 0.07;
                if (opacity > 1f) {opacity = 1f;}
                g2D.setColor(new Color(drawColor.getRed() / 255f, drawColor.getGreen() / 255f, drawColor.getBlue() / 255f, opacity));
                g2D.fillRoundRect(getWidth() - 33 + i, 3 + (i), (int) (30.5f - (i * 2)), 19 - (i * 2), 15, 15);
            }

            g2D.setColor(drawColor);

        }
        g2D.fillRoundRect(getWidth() - 28, 6, 20, 13, 9, 9);

        recolor();
    }

    public PlayDataItem(String name, String _timerIn, String _timerOut, Boolean _repeat) {
        alarmList = new JList(arm);

        setName(name);
        setFont(Registry.trackSelectedFont);
        this.timerIn = _timerIn;
        this.timerOut = _timerOut;
        this.repeat = _repeat;
        secondColor = defBkgColor;
        if (getName().equals("Saturday") || getName().equals("Sunday")) {
            secondColor = defBkgColor = Color.CYAN;
        }
        playpane = new PlayPane(this);

        setOpaque(false);
        setLayout(new BorderLayout(0,0));

        dayNameLabel = new JLabel(getName()) {{setFont(Registry.titleFont); setBorder(new EmptyBorder(3,6,0,0));}};

        dayControlPane = new JPanel(new BorderLayout()) {
            {
                setOpaque(false);
                setBorder(new EmptyBorder(0,3,1,3));

                JPanel upSchedulePane = new JPanel(new GridLayout(2,1)) {
                    {
                        setOpaque(false);

                        JPanel inTimePane = new JPanel(new GridLayout(1,3, 6,0)) {
                            {
                                setOpaque(false);

                                JPanel pane01 = new JPanel(new BorderLayout(0,0)) {
                                    {
                                        setOpaque(false);

                                        inLabelH = new JLabel("In-Hour:") {{setHorizontalAlignment(JLabel.LEFT);}};
                                        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(8,0,23,1)) {
                                            {
                                                setEditor(new JSpinner.NumberEditor(this,"00"));
                                                setValue(Integer.parseInt(timerIn.split(":")[0]));
                                                getEditor().setBackground(Color.BLACK);
                                                addChangeListener(e -> timerIn = getValue() + ":" + timerIn.split(":")[1] + ":" + timerIn.split(":")[2]);
                                            }
                                        };

                                        add(inLabelH, BorderLayout.NORTH);
                                        add(hourSpinner, BorderLayout.CENTER);
                                    }
                                };

                                JPanel pane02 = new JPanel(new BorderLayout(0,0)) {
                                    {
                                        setOpaque(false);

                                        inLabelM = new JLabel("In-Min.:") {{setHorizontalAlignment(JLabel.LEFT);}};
                                        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,1)) {
                                            {
                                                setEditor(new JSpinner.NumberEditor(this,"00"));
                                                setValue(Integer.parseInt(timerIn.split(":")[1]));
                                                getEditor().setBackground(Color.BLACK);
                                                addChangeListener(e -> timerIn = timerIn.split(":")[0] + ":" + getValue() + ":" + timerIn.split(":")[2]);
                                            }
                                        };

                                        add(inLabelM, BorderLayout.NORTH);
                                        add(minuteSpinner, BorderLayout.CENTER);
                                    }
                                };

                                JPanel pane03 = new JPanel(new BorderLayout(0,0)) {
                                    {
                                        setOpaque(false);

                                        inLabelS = new JLabel("In-Sec.:") {{setHorizontalAlignment(JLabel.LEFT);}};
                                        JSpinner secondSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,5)) {
                                            {
                                                setEditor(new JSpinner.NumberEditor(this,"00"));
                                                setValue(Integer.parseInt(timerIn.split(":")[2]));
                                                getEditor().setBackground(Color.BLACK);
                                                addChangeListener(e -> timerIn = timerIn.split(":")[0] + ":" + timerIn.split(":")[1] + ":" + getValue());
                                            }
                                        };

                                        add(inLabelS, BorderLayout.NORTH);
                                        add(secondSpinner, BorderLayout.CENTER);
                                    }
                                };

                                add(pane01);
                                add(pane02);
                                add(pane03);
                            }
                        };

                        JPanel outTimePane = new JPanel(new GridLayout(1,3, 6, 0)) {
                            {
                                setOpaque(false);

                                JPanel pane01 = new JPanel(new BorderLayout(0,0)) {
                                    {
                                        setOpaque(false);

                                        outLabelH = new JLabel("Out-Hour:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(8,0,23,1)) {
                                            {
                                                setEditor(new JSpinner.NumberEditor(this,"00"));
                                                setValue(Integer.parseInt(timerOut.split(":")[0]));
                                                getEditor().setBackground(Color.BLACK);
                                                addChangeListener(e -> timerOut = getValue() + ":" + timerOut.split(":")[1] + ":" + timerOut.split(":")[2]);
                                            }
                                        };
                                        add(outLabelH, BorderLayout.NORTH);
                                        add(hourSpinner, BorderLayout.CENTER);
                                    }
                                };

                                JPanel pane02 = new JPanel(new BorderLayout(0,0)) {
                                    {
                                        setOpaque(false);

                                        outLabelM = new JLabel("Out-Min.:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,1)) {
                                            {
                                                setEditor(new JSpinner.NumberEditor(this,"00"));
                                                setValue(Integer.parseInt(timerOut.split(":")[1]));
                                                getEditor().setBackground(Color.BLACK);
                                                addChangeListener(e -> timerOut = timerOut.split(":")[0] + ":" + getValue() + ":" + timerOut.split(":")[2]);
                                            }
                                        };
                                        add(outLabelM, BorderLayout.NORTH);
                                        add(minuteSpinner, BorderLayout.CENTER);
                                    }
                                };

                                JPanel pane03 = new JPanel(new BorderLayout(0,0)) {
                                    {
                                        setOpaque(false);

                                        outLabelS = new JLabel("Out-Sec.:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                        JSpinner secondSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,5)) {
                                            {
                                                setEditor(new JSpinner.NumberEditor(this,"00"));
                                                setValue(Integer.parseInt(timerOut.split(":")[2]));
                                                getEditor().setBackground(Color.BLACK);
                                                addChangeListener(e -> timerOut = timerOut.split(":")[0] + ":" + timerOut.split(":")[1] + ":" + getValue());
                                            }
                                        };
                                        add(outLabelS, BorderLayout.NORTH);
                                        add(secondSpinner, BorderLayout.CENTER);
                                    }
                                };

                                add(pane01);
                                add(pane02);
                                add(pane03);
                            }
                        };

                        add(inTimePane);
                        add(outTimePane);
                    }
                };

                JPanel downOptionsPane = new JPanel(new BorderLayout(1,0)) {
                    {
                        setOpaque(false);
                        setBorder(new EmptyBorder(6,0,0,3));

                        alarmsBtn = new JButton() {
                            BufferedImage im;

                            {
                                try {im = ImageIO.read(new File("./resources/icons/alarm.png"));
                                } catch (IOException e) {e.printStackTrace();}
                            }

                            @Override
                            public void paintComponent(Graphics g) {
                                if (im != null) {
                                    g.setColor(alarmsBack);
                                    g.fillRoundRect(0,0,32,32,6,6);

                                    g.drawImage(im, 3, 3, 26, 26, null);

                                    g.setColor(Color.DARK_GRAY);
                                    g.drawRoundRect(1,1,29,29,3,3);
                                } else {super.paintComponent(g);}
                            }

                            {
                                try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/alarm.png").toUri().toURL())));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                setPreferredSize(new Dimension(32, 32));
                                setActionCommand("alarmBtn");
                                addActionListener(PlayDataItem.this);
                            }
                        };

                        JPanel btnsPane = new JPanel(new GridLayout(1,0)) {
                            {
                                setOpaque(false);

                                startPlayBtn = new JButton() {
                                    BufferedImage im;

                                    @Override
                                    public void paintComponent(Graphics g) {
                                        try {
                                            if (isPlaying) {
                                                im = ImageIO.read(new File("./resources/icons/playPressed.png"));
                                            } else {
                                                im = ImageIO.read(new File("./resources/icons/play.png"));
                                            }
                                        } catch (IOException e) {e.printStackTrace();}

                                        g.drawImage(im, 1, 1, getWidth() - 3, downButtonsDim, null);
                                    }

                                    {
                                        setFont(Registry.btnsFont2);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/play.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        setActionCommand("play");
                                        addActionListener(PlayDataItem.this);
                                    }
                                };

                                nextPlayBtn = new JButton() {
                                    BufferedImage im;

                                    {
                                        try {im = ImageIO.read(new File("./resources/icons/next.png"));
                                        } catch (IOException e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void paintComponent(Graphics g) {
                                        if (im != null) {
                                            g.drawImage(im, 1, 1, getWidth() - 3, downButtonsDim, null);
                                        } else {super.paintComponent(g);}
                                    }
                                    {
                                        setFont(Registry.btnsFont2);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/next.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        setActionCommand("next");
                                        addActionListener(PlayDataItem.this);
                                    }
                                };

                                stopPlayBtn = new JButton() {
                                    BufferedImage im;

                                    {
                                        try {im = ImageIO.read(new File("./resources/icons/stop.png"));
                                        } catch (IOException e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void paintComponent(Graphics g) {
                                        if (im != null) {
                                            g.drawImage(im, 1, 1, getWidth() - 3, downButtonsDim, null);
                                        } else {super.paintComponent(g);}
                                    }
                                    {
                                        setFont(Registry.btnsFont2);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/stop.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        setActionCommand("stop");
                                        addActionListener(PlayDataItem.this);
                                    }
                                };

                                add(startPlayBtn);
                                add(nextPlayBtn);
                                add(stopPlayBtn);
                            }
                        };

                        JPanel repeatPane = new JPanel(new BorderLayout(0,0)) {
                            {
                                setOpaque(false);

                                repeatCBox = new JCheckBox("Повтор") {
                                    {
                                        setFont(Registry.btnsFont2);
                                        setForeground(defTextColor);
                                        setSelected(repeat);
                                        addItemListener(new ItemListener() {
                                            public void itemStateChanged(ItemEvent e) {
                                                repeat = isSelected();
                                            }
                                        });
                                    }
                                };

                                add(repeatCBox, BorderLayout.EAST);
                            }
                        };

                        add(alarmsBtn, BorderLayout.WEST);
                        add(btnsPane, BorderLayout.CENTER);
                        add(repeatPane, BorderLayout.EAST);
                    }
                };

                add(upSchedulePane, BorderLayout.CENTER);
                add(downOptionsPane, BorderLayout.SOUTH);
            }
        };

        add(dayNameLabel, BorderLayout.NORTH);
        add(dayControlPane, BorderLayout.CENTER);

        setBorder(new EmptyBorder(3,3,3,3));
        addMouseListener(this);
    }

    public synchronized void saveToFile() {
        try {
            Out.Print(PlayDataItem.class, Out.LEVEL.INFO, "Try to save " + getName() + " data...");

            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".meta"), StandardCharsets.UTF_8)) {
                osw.write(
                        "NN_T_IN_EE" + timerIn +
                            "NN_T_OUT_EE" + timerOut +
                            "NN_REP_EE" + repeat
                );
            } catch (Exception e) {
                Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "PlayDataItem cant reached save to .meta: " + e.getMessage());
                e.printStackTrace();
            }

            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".alarms"), StandardCharsets.UTF_8)) {
                for (int i = 0; i < arm.size(); i++) {
                    osw.write(arm.get(i).getTime() + ">" + arm.get(i).getTrack() + "\r\n");
                }
            } catch (Exception e) {
                Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "PlayDataItem cant reached save to .alarms: " + e.getMessage());
                e.printStackTrace();
            }

            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".list"), StandardCharsets.UTF_8)) {

                for (Path track : playpane.getTracks()) {
                    osw.write(track.toString() + "\r\n");
                }

            } catch (Exception e) {
                Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "PlayDataItem cant reached save to .list: " + e.getMessage());
                e.printStackTrace();
            }

            Out.Print(PlayDataItem.class, Out.LEVEL.INFO, "Saving day " + getName() + " successfully accomplished!");
        } catch (Exception e) {
            e.printStackTrace();
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "Error with saving PlayDataItemMy: " + e.getMessage());
        }
    }


    // Audio control:
    public synchronized void play(int index) {
        if (index != -1) {indexOfPlayed = index;}

        if (playpane == null || playpane.getTrack(indexOfPlayed) == null) {
            JOptionPane.showConfirmDialog(this, "Плейлист пуст!", "Инфо:", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }
        if (indexOfPlayed == -1) {return;}
        if (isPlaying || (alarmThread != null && alarmThread.isAlive())) {stop();}

        try {
            Path tr = playpane.getTrack(indexOfPlayed);
            Out.Print(PlayDataItem.class, Out.LEVEL.DEBUG, "The track '" + tr.toFile().getName() + "' is played now...");
            musicThread = new Thread(() -> {
                repaint();
                while (indexOfPlayed < playpane.getRowsCount()) {
                    BackVocalFrame.updatePlayedLabelText();

                    File playFile = playpane.getTrack(indexOfPlayed).toFile();
                    try (BufferedInputStream mp3 = new BufferedInputStream(new FileInputStream(playFile))) {
                        isPlaying = true;
                        player = new Player(mp3, FactoryRegistry.systemRegistry().createAudioDevice());
                        player.play();
                    } catch (Exception e) {
                        Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "The track '" + playFile + "' can`t be found.");
                        e.printStackTrace();
                        playpane.setFallTrack(indexOfPlayed);
                    } finally {
                        try {
                            player.close();
                        } catch (Exception e) {/* IGNORE */}
                    }

                    indexOfPlayed++;
                    if (repeat && !isHandStopped) {
                        if (indexOfPlayed >= playpane.getRowsCount()) {
                            System.out.println("REPEAT INITIATED!");
                            indexOfPlayed = 0;
                        }
                    }
                }
            });
            musicThread.start();
        } catch (Exception e) {
            Out.Print(PlayDataItem.class, Out.LEVEL.ACCENT, "Play-method exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void playNext() {
        if (indexOfPlayed >= playpane.getRowsCount()) {
            indexOfPlayed = 0;
        }
        if (playpane.getRowsCount() > 0) {
            play(-1);
        }
    }

    public synchronized void stop() {
        System.out.println("STOPPED!");

        try {
            player.close();
//            pausedOnFrame = player.getPosition();
        } catch (Exception e) {/* IGNORE BY NOW */}

        try {
            musicThread.interrupt();
            musicThread.stop();
        } catch (Exception e) {/* IGNORE BY NOW */}

        try {
            alarmThread.interrupt();
            alarmThread.stop();
        } catch (Exception e) {/* IGNORE BY NOW */}

        isPlaying = false;

        BackVocalFrame.updatePlayedLabelText();

        playpane.repaint();
    }

    public synchronized void playAlarm(Path alarmFilePath) {
        BackVocalFrame.setPlayedLabelText("<html><b color='RED'>Alarm:</b> " + alarmFilePath.toFile().getName());

        alarmThread = new Thread(() -> {
            alarmIsPlayed = true;
            BackVocalFrame.getFrame().repaint();

            try {
                URI uri = alarmFilePath.toFile().toURI();
                try (
                        InputStream s = uri.toURL().openStream();
                        BufferedInputStream mp3 = new BufferedInputStream(s)) {
                    new AdvancedPlayer(mp3).play();
                }
            } catch (IOException | JavaLayerException e) {
                e.printStackTrace();
            } finally {
                alarmIsPlayed = false;
            }
        });
        alarmThread.start();
    }


    // Getters & setters:
    public boolean isHandStopped() {
        return isHandStopped;
    }

    public void setHandStopped(boolean isHandStopped) {
        this.isHandStopped = isHandStopped;
    }

    public void addTracks(List<Path> path) {
        try {
            playpane.add(path);
        } catch (Exception e) {
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "Exception by adding track: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green.darker() : null);

        if (selected) {
            BackVocalFrame.showPlayList(playpane);

            if (playpane != null && playpane.getRowsCount() > 0) {
                BackVocalFrame.updatePlayedLabelText();
            }
        }

        BackVocalFrame.enableControls(selected);
        playpane.repaint();
    }
    public boolean isSelected() {return this.isSelected;}

    public boolean isPlayed() {
        return isPlaying;
    }

    public String getActiveTrackName() {
        return playpane.getTrack(indexOfPlayed).toFile().getName();
    }


    public void moveSelectedUp() {
        if (indexOfPlayed == playpane.getSelectedIndex()) {
            if (playpane.moveSelectedUp()) {
                indexOfPlayed--;
            }
        } else {
            playpane.moveSelectedUp();
        }
    }

    public void removeSelected() {
        int[] selected = getSelectedIndexes();
        for (int i = 0; i < selected.length; i++) {
            if (indexOfPlayed == playpane.getSelectedIndex()) {
                playpane.getOwner().stop();
            } else {
                if (indexOfPlayed > playpane.getSelectedIndex()) {
                    indexOfPlayed--;
                }
            }
            playpane.removeSelected();
        }
    }

    public void moveSelectedDown() {
        if (playpane.getSelectedIndex() == -1) {return;}

        if (indexOfPlayed == playpane.getSelectedIndex()) {
            if (playpane.moveSelectedDown()) {
                indexOfPlayed++;
            }
        } else {
            playpane.moveSelectedDown();
        }
    }


    public PlayPane getPlayPane() {return playpane;}

    public void addAlarm(String time, Path track) {
        arm.addElement(new AlarmItem(time, track));
        alarmsBack = Color.GREEN;
    }

    public ArrayList<AlarmItem> getAlarmData() {
        ArrayList<AlarmItem> result = new ArrayList<>();

        for (int i = 0; i < arm.size(); i++) {
            result.add(arm.get(i));
        }

        return result;
    }

    public boolean inSchedulingTimeAccept() {
        date = LocalDateTime.now();

        String now = "", on = "", off = "";
        String[] nowNotParsed = date.toString().split("T")[1].split("\\.")[0].split(":");
        for (String s : nowNotParsed) {
            if (s.length() == 1) {s = "0" + s;}
            now += s;
        }
        String[] onNotParsed = timerIn.split(":");
        for (String s : onNotParsed) {
            if (s.length() == 1) {s = "0" + s;}
            on += s;
        }
        String[] offNotParsed = timerOut.split(":");
        for (String s : offNotParsed) {
            if (s.length() == 1) {s = "0" + s;}
            off += s;
        }


        int nowParsed = Integer.parseInt(now);
        int inParsed = Integer.parseInt(on);
        int outParsed = Integer.parseInt(off);
//        System.out.println("Now: " + nowParsed + "; On: " + inParsed + "; Off: " + outParsed);

        if (nowParsed < inParsed || nowParsed >= outParsed) {
//            System.out.println("FALSE becouse: " + (nowParsed < inParsed ? "now < on" : nowParsed >= outParsed ? "now >= off" : "unknown") + " (now: " + nowParsed + ", off: " + outParsed + ")");
            return false;
        }

        return true;
    }

    public boolean isTimeCome(String timeNotParsed) {
        String time = "";
        String[] timeNotParsedArr = timeNotParsed.split(":");
        for (String s : timeNotParsedArr) {
            if (s.length() == 1) {s = "0" + s;}
            time += s;
        }

        String nowTime = LocalDateTime.now().toString();
        int now = Integer.parseInt(nowTime.split("T")[1].split("\\.")[0].replaceAll(":", ""));
        int need = Integer.parseInt(time);
//        System.out.println("Now: " + now + "; Need: " + need);

        if (now > need && now - need < 100) {
            return true;
        }

        return false;
    }

    public int getIndexOfPlayed() {return indexOfPlayed;}

    public boolean isAlarmPlayed() {return alarmIsPlayed;}

    public int[] getSelectedIndexes() {
        return playpane.getSelectedIndexes();
    }

    public boolean isToday() {
        return getName().equalsIgnoreCase(LocalDateTime.now().getDayOfWeek().name());
    }


    // subframes:
    private class AlarmsDialog extends JDialog {

        public AlarmsDialog(JFrame parent) {
            super(parent, "Лист оповещений:", true);

            setMinimumSize(new Dimension(400, 400));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel basePane = new JPanel() {
                {
                    setBackground(Color.MAGENTA);
                    setLayout(new BorderLayout());

                    alarmList.setBackground(Color.DARK_GRAY);
                    alarmList.setForeground(Color.WHITE);

                    JScrollPane centerAlarmsListPane = new JScrollPane(alarmList) {
                        {
                            setBorder(null);
                        }
                    };

                    JPanel downButtonsPane = new JPanel(new BorderLayout(3, 0)) {
                        {
                            setBackground(Color.DARK_GRAY);
                            setBorder(new EmptyBorder(0,3,3,3));

                            JButton addAlarmBtn = new JButton("+ alarm") {
                                {
                                    setBackground(new Color(0.75f,1.0f,0.75f,1.0f));
                                    addActionListener(e -> {
                                        String now = LocalDateTime.now().toString().split("T")[1].split("\\.")[0];
                                        String alarmInitTime = JOptionPane.showInputDialog(AlarmsDialog.this,
                                                        "Старт в:", now);
                                        if (alarmInitTime.isBlank()) {return;}

                                        alarmInitTime = alarmInitTime.trim();

                                        fch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                        fch.setMultiSelectionEnabled(false);
                                        fch.setDialogTitle("Выбери alarm:");

                                        Path alarmFilePath;
                                        int result = fch.showOpenDialog(AlarmsDialog.this);
                                        if (result == JFileChooser.APPROVE_OPTION ) {
                                            try {
                                                alarmFilePath = fch.getSelectedFile().toPath();

                                                if (!alarmInitTime.isBlank() &&
                                                        alarmInitTime.length() == 8 &&
                                                        alarmInitTime.contains(":") && !alarmInitTime.contains(";") &&
                                                        alarmInitTime.split(":").length == 3 &&
                                                        Integer.parseInt(alarmInitTime.split(":")[0]) < 23 &&
                                                        Integer.parseInt(alarmInitTime.split(":")[1]) < 59 &&
                                                        Integer.parseInt(alarmInitTime.split(":")[2]) < 59
                                                ) {
                                                    arm.addElement(new AlarmItem(alarmInitTime, alarmFilePath));
                                                    alarmsBack = Color.GREEN;
                                                } else {
                                                    JOptionPane.showConfirmDialog(AlarmsDialog.this, "Ошибка ввода!", "Отменено.", JOptionPane.DEFAULT_OPTION);
                                                }
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            };

                            JButton remAlarmBtn = new JButton("- alarm") {
                                {
                                    setBackground(new Color(1.0f,0.75f,0.75f,1.0f));
                                    addActionListener(e -> deleteAlarmRequest());
                                }
                            };

                            add(addAlarmBtn, BorderLayout.CENTER);
                            add(remAlarmBtn, BorderLayout.EAST);
                        }
                    };

                    add(centerAlarmsListPane, BorderLayout.CENTER);
                    add(downButtonsPane, BorderLayout.SOUTH);
                }
            };

            add(basePane);

            InputAction.add("alDia", basePane);
            InputAction.set("alDia", "deleteAlarm", KeyEvent.VK_DELETE, 0, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getID() == 1001) {
                        deleteAlarmRequest();
                    }
                }
            });
            InputAction.set("alDia", "close", KeyEvent.VK_ESCAPE, 0, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void deleteAlarmRequest() {
            AlarmItem toDelete = alarmList.getSelectedValue();
            if (toDelete == null) {return;}
            int req = JOptionPane.showConfirmDialog(AlarmsDialog.this,
                    "Удалить alarm от " + toDelete.getTime() + "?", "Уверен:", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (req == 0) {
                arm.removeElement(toDelete);
                if (arm.size() == 0) {
                    alarmsBack = Color.DARK_GRAY;
                }
            }
        }
    }


    // Listeners:
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {

            case "alarmBtn": new AlarmsDialog(BackVocalFrame.getFrame());
                break;

            case "play":
                if (isAlarmPlayed()) {return;}

                if (inSchedulingTimeAccept()) {
                    isHandStopped = false;
                    stop();

                    if (playpane.getSelectedIndex() != playpane.getPlayedIndex()) {
                        indexOfPlayed = playpane.getSelectedIndex();
                        play(-1);
                        return;
                    }

                    play(-1);
                    BackVocalFrame.resetDownPaneSelect();
                    setSelected(true);
                } else {
                    new FOptionPane("Тайм-аут:", "Время воспроизведения истекло!", FOptionPane.TYPE.DEFAULT, null);
                }
                break;

            case "next":
                if (isAlarmPlayed()) {return;}

                if (inSchedulingTimeAccept()) {
                    stop();
                    playNext();
                    BackVocalFrame.resetDownPaneSelect();
                    setSelected(true);
                }
                break;

            case "stop":
                if (isAlarmPlayed()) {
                    stop();
                    BackVocalFrame.resetDownPaneSelect();
                    setSelected(true);
                    play(-1);
                    return;
                }
                isHandStopped = true;
                stop();
                BackVocalFrame.resetDownPaneSelect();
                setSelected(true);
                break;



            default:
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isSelected) {
            BackVocalFrame.resetDownPaneSelect();
            return;
        }
        BackVocalFrame.resetDownPaneSelect();
        setSelected(true);
        BackVocalFrame.getFrame().repaint();
    }
    @Override
    public void mouseEntered(MouseEvent e) {
        isOver = true;
        defBkgColor = Color.YELLOW;
        defTextColor = Color.BLACK;
        repaint();
    }
    @Override
    public void mouseExited(MouseEvent e) {
        isOver = false;
        defBkgColor = secondColor;
        repaint();
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}


    // other:
    @Override
    public void setBackground(Color bg) {
        super.setBackground(isSelected ? Color.GREEN : bg == null ? defBkgColor : bg);
    }

    @Override
    public String toString() {
        return "PDate item '" + getName() + "' (" + playpane.getRowsCount() + " tracks)";
    }

    private void recolor() {
        dayNameLabel.setForeground(defTextColor);
        inLabelM.setForeground(defTextColor);
        inLabelS.setForeground(defTextColor);
        inLabelH.setForeground(defTextColor);
        outLabelH.setForeground(defTextColor);
        outLabelM.setForeground(defTextColor);
        outLabelS.setForeground(defTextColor);
        repeatCBox.setForeground(defTextColor);
    }
}
