package gui;

import fox.components.AlarmItem;
import fox.components.AlarmItemCycle;
import fox.components.PlayPane;
import fox.components.iAlarm;
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
    private PlayPane playPane;

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

    private static int indexCounter = 0;
    private int index;

    private DefaultListModel<iAlarm> arm = new DefaultListModel();
    private JList<AlarmItem> alarmList;
    private LocalDateTime date;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = FoxRender.setMedRender((Graphics2D) g);

        // backbround:
        if (playPane == null || playPane.isEmpty()) {g2D.setColor(defBkgColor);
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
        if (playPane == null || playPane.isEmpty()) {
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

        index = indexCounter;
        indexCounter++;

        setName(name);
        setFont(Registry.trackSelectedFont);
        this.timerIn = _timerIn;
        this.timerOut = _timerOut;
        this.repeat = _repeat;
        secondColor = defBkgColor;
        if (getName().equals("Saturday") || getName().equals("Sunday")) {
            secondColor = defBkgColor = Color.CYAN;
        }
        playPane = new PlayPane(this);

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

            int counter = 0;
            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".alarms"), StandardCharsets.UTF_8)) {
                for (int i = 0; i < arm.size(); i++) {
                    osw.write(arm.get(i).getTime() + ">" + arm.get(i).getTrack() + ">" + arm.get(i).isCycled() + "\r\n");
                    counter++;
                }
                Out.Print(PlayDataItem.class, Out.LEVEL.INFO, "Saved alarms: " + counter);
            } catch (Exception e) {
                Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "PlayDataItem cant reached save to .alarms: " + e.getMessage());
                e.printStackTrace();
            }

            counter = 0;
            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".list"), StandardCharsets.UTF_8)) {

                for (Path track : playPane.getTracks()) {
                    osw.write(track.toString() + "\r\n");
                    counter++;
                }

                Out.Print(PlayDataItem.class, Out.LEVEL.INFO, "Saved tracks: " + counter);
            } catch (Exception e) {
                Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "PlayDataItem cant reached save to .list: " + e.getMessage());
                e.printStackTrace();
            }

            Out.Print(PlayDataItem.class, Out.LEVEL.INFO, "Saving day " + getName() + " successfully accomplished!");
        } catch (Exception e) {
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "Error with saving PlayDataItem: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Audio control:
    public synchronized void play(int index) {
        if (index != -1) {indexOfPlayed = index;}
        if (indexOfPlayed == -1) {return;}

        if (playPane == null || playPane.isEmpty()) {
            JOptionPane.showConfirmDialog(
                    this, "Плейлист пуст!", "Инфо:",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }

        if (isPlaying || (alarmThread != null && alarmThread.isAlive())) {stop();}

        try {
            Path tr = playPane.getTrack(indexOfPlayed);
            Out.Print(PlayDataItem.class, Out.LEVEL.DEBUG, "The track '" + tr.toFile().getName() + "' is played now...");
            musicThread = new Thread(() -> {
                repaint();
                while (indexOfPlayed < playPane.getRowsCount()) {
                    BackVocalFrame.updatePlayedLabelText();

                    File playFile = playPane.getTrack(indexOfPlayed).toFile();
                    try (BufferedInputStream mp3 = new BufferedInputStream(new FileInputStream(playFile))) {
                        isPlaying = true;
                        player = new Player(mp3, FactoryRegistry.systemRegistry().createAudioDevice());
                        player.play();
                    } catch (Exception e) {
                        Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "The track '" + playFile + "' can`t be found.");
                        e.printStackTrace();
                        playPane.setFallTrack(indexOfPlayed);
                    } finally {
                        try {
                            player.close();
                        } catch (Exception e) {/* IGNORE */}
                    }

                    indexOfPlayed++;
                    if (repeat && !isHandStopped) {
                        if (indexOfPlayed >= playPane.getRowsCount()) {
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
        if (indexOfPlayed >= playPane.getRowsCount()) {
            indexOfPlayed = 0;
        }
        if (playPane.getRowsCount() > 0) {
            play(-1);
        }
    }

    public synchronized void stop() {
        try {player.close();
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

        playPane.repaint();
    }

    public synchronized void playAlarm(Path alarmFilePath) {
        BackVocalFrame.setPlayedLabelText("<html><b color='RED'>Alarm:</b> " + alarmFilePath.toFile().getName());

        try {
            if ((musicThread != null && !musicThread.isInterrupted()) || (alarmThread != null && !alarmThread.isInterrupted()) || isPlaying) {
                stop();
            }
        } catch (Exception e) {
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "playAlarm(): Play stop exception: " + e.getMessage());
            e.printStackTrace();
        }

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
            playPane.add(path);
        } catch (Exception e) {
            Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "Exception by adding track: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green.darker() : null);

        if (selected) {
            BackVocalFrame.showPlayList(playPane);

            if (playPane != null && playPane.getRowsCount() > 0) {
                BackVocalFrame.updatePlayedLabelText();
            }
        }

        BackVocalFrame.enableControls(selected);
        playPane.repaint();
    }
    public boolean isSelected() {return this.isSelected;}

    public boolean isPlayed() {
        return isPlaying;
    }

    public String getActiveTrackName() {
        return playPane.getTrack(indexOfPlayed).toFile().getName();
    }


    public void moveSelectedUp() {
        if (indexOfPlayed == playPane.getSelectedIndex()) {
            if (playPane.moveSelectedUp()) {
                indexOfPlayed--;
            }
        } else {
            playPane.moveSelectedUp();
        }
    }

    public void removeSelected() {
        int[] selected = getSelectedIndexes();
        for (int i = 0; i < selected.length; i++) {
            if (indexOfPlayed == playPane.getSelectedIndex()) {
                playPane.getOwner().stop();
            } else {
                if (indexOfPlayed > playPane.getSelectedIndex()) {
                    indexOfPlayed--;
                }
            }
            playPane.removeSelected();
        }
    }

    public void moveSelectedDown() {
        if (playPane.getSelectedIndex() == -1) {return;}

        if (indexOfPlayed == playPane.getSelectedIndex()) {
            if (playPane.moveSelectedDown()) {
                indexOfPlayed++;
            }
        } else {
            playPane.moveSelectedDown();
        }
    }


    public PlayPane getPlayPane() {return playPane;}

    public void addAlarm(String time, Path track, boolean isCycled) {
        if (isCycled) {
            arm.addElement(new AlarmItemCycle(time, track));
        } else {
            arm.addElement(new AlarmItem(time, track));
        }
        alarmsBack = Color.GREEN;
    }

    public ArrayList<iAlarm> getAlarmData() {
        ArrayList<iAlarm> result = new ArrayList<>();

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

    public boolean isTimeCome(iAlarm alarm) {
        String nowTime = LocalDateTime.now().toString();
        String lastTime;

        if (alarm.getTime().startsWith("R")) {
            AlarmItemCycle tmp = (AlarmItemCycle) alarm;
            lastTime = tmp.getStartTime().toString();

            int now = Integer.parseInt(nowTime.split("T")[1].split("\\.")[0].replaceAll(":", ""));
            int last = Integer.parseInt(lastTime.split("T")[1].split("\\.")[0].replaceAll(":", ""));
            int need = last + Integer.parseInt(tmp.getTime().replace("R", "") + "00");
            System.out.println("NOW: " + now + "; NEED: " + need);
            if (now > need) {
                tmp.resetStartTime();
                return true;
            }
        } else {
            String time = "";
            String[] timeNotParsedArr = alarm.getTime().split(":");
            for (String s : timeNotParsedArr) {
                if (s.length() == 1) {
                    s = "0" + s;
                }
                time += s;
            }

            int now = Integer.parseInt(nowTime.split("T")[1].split("\\.")[0].replaceAll(":", ""));
            int need = Integer.parseInt(time);

            if (now > need && now - need < 100) {
                return true;
            }
        }

        return false;
    }

    public int getIndexOfPlayed() {return indexOfPlayed;}

    public boolean isAlarmPlayed() {return alarmIsPlayed;}

    public int[] getSelectedIndexes() {
        return playPane.getSelectedIndexes();
    }

    public boolean isToday() {
        return getName().equalsIgnoreCase(LocalDateTime.now().getDayOfWeek().name());
    }


    // subframes:
    private class AlarmsDialog extends JDialog {

        public AlarmsDialog(JFrame parent) {
            super(parent, "Лист оповещений:", true);

            setMinimumSize(new Dimension(600, 450));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel basePane = new JPanel() {
                {
                    setBackground(Color.BLACK);
                    setLayout(new BorderLayout(3,3));

                    alarmList.setBackground(Color.BLACK);
                    alarmList.setForeground(Color.WHITE);
                    alarmList.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(
                                    BorderFactory.createLineBorder(Color.DARK_GRAY, 2, true),
                                    "Оповещения:",
                                    1, 2, Registry.titleFont, Color.WHITE
                            ),
                            new EmptyBorder(3,3,3,3)
                    ));

                    JScrollPane centerAlarmsListPane = new JScrollPane(alarmList) {
                        {
                            setBorder(null);
                        }
                    };

                    JPanel downButtonsPane = new JPanel(new BorderLayout(3, 0)) {
                        {
                            setBackground(Color.BLACK);
                            setBorder(new EmptyBorder(0,3,3,3));

                            JButton addAlarmBtn = new JButton("+ alarm") {
                                {
                                    setBackground(new Color(0.75f,1.0f,0.75f,1.0f));
                                    addActionListener(e -> {
                                        String now = LocalDateTime.now().toString().split("T")[1].split("\\.")[0];
                                        String alarmInitTime = JOptionPane.showInputDialog(AlarmsDialog.this,
                                                        "Старт в (вводи R10 для повтора каждые 10 мин):", now);
                                        if (alarmInitTime == null || alarmInitTime.isBlank()) {return;}

                                        alarmInitTime = alarmInitTime.trim();

                                        fch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                        fch.setMultiSelectionEnabled(false);
                                        fch.setDialogTitle("Выбери alarm:");

                                        Path alarmFilePath;
                                        int result = fch.showOpenDialog(AlarmsDialog.this);
                                        if (result == JFileChooser.APPROVE_OPTION ) {
                                            try {
                                                alarmFilePath = fch.getSelectedFile().toPath();

                                                int mode = checkAlarmTime(alarmInitTime);
                                                if (mode == 0) {
                                                    addAlarm(alarmInitTime, alarmFilePath, false);
                                                } else if (mode == 1) {
                                                    addAlarm(alarmInitTime, alarmFilePath, true);
                                                    if (inSchedulingTimeAccept()) {
                                                        playAlarm(alarmFilePath);
                                                    }
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

                        private int checkAlarmTime(String alarmInitTime) {
                            if (alarmInitTime.isBlank()) {return -1;}

                            if (alarmInitTime.length() == 8 &&
                                    alarmInitTime.contains(":") && !alarmInitTime.contains(";") &&
                                    alarmInitTime.split(":").length == 3 &&
                                    Integer.parseInt(alarmInitTime.split(":")[0]) < 23 &&
                                    Integer.parseInt(alarmInitTime.split(":")[1]) < 59 &&
                                    Integer.parseInt(alarmInitTime.split(":")[2]) < 59) {
                                return 0;
                            } else if (alarmInitTime.startsWith("R")) {
                                try {
                                    Integer.parseInt(alarmInitTime.substring(1));
                                    return 1;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return -1;
                                }
                            } else {
                                return -1;
                            }
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
            iAlarm toDelete = alarmList.getSelectedValue();
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

                    if (playPane.getSelectedIndex() != playPane.getPlayedIndex()) {
                        indexOfPlayed = playPane.getSelectedIndex();
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
        return "PDate item '" + getName() + "' (" + playPane.getRowsCount() + " tracks)";
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
