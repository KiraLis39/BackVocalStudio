package gui;

import fox.components.PlayDataItem;
import fox.components.Playlist;
import door.MainClass;
import fox.fb.FoxFontBuilder;
import fox.out.Out;
import registry.Codes;
import registry.Registry;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.event.*;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BackVocalFrame extends JFrame implements WindowListener {
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static ExecutorService executor;

    private static JPanel basePane, centerPlaylistsPane, playDatePane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn, moveUpBtn, moveDownBtn, removeBtn;
    private static JLabel nowPlayedLabel1;
    private static JProgressBar playProgress;

    private static PlayDataItem[] dayItems = new PlayDataItem[7];
    private static String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private static Font headersFontSmall = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private static Font btnsFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, false);
    private static Font btnsFont2 = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private static BackVocalFrame frame;


    public BackVocalFrame() {
        frame = this;

        Out.Print("Build the frame...");

        try {setIconImage(new ImageIcon(ImageIO.read(new File("./resources/icons/0.png"))).getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        basePane = new JPanel(new BorderLayout(3,3)) {
            {
                setBackground(Color.DARK_GRAY);

                centerPlaylistsPane = new JPanel(new BorderLayout(3,3)) {
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
                        getVerticalScrollBar().setUnitIncrement(16);
                    }
                };

                JPanel downShedulePane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setBackground(Color.gray);
//                        setPreferredSize(new Dimension(400, 0));

                        playDatePane = new JPanel(new GridLayout(1, 0, 3,0)) {
                            {
                                setBackground(Color.BLACK);
                            }
                        };

                        playDateScroll = new JScrollPane(playDatePane) {
                            {
                                setBorder(null);
                                getViewport().setPreferredSize(new Dimension(BackVocalFrame.this.getWidth(), 160));
//                                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                getVerticalScrollBar().setUnitIncrement(8);
                            }
                        };

                        JPanel downBtnsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                            {
                                setOpaque(false);

                                bindListBtn = new JButton("Bind to dir") {
                                    {
                                        setFont(btnsFont);
                                        setEnabled(false);
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                Component[] comps = playDatePane.getComponents();

                                                JFileChooser fch = new JFileChooser("./resources/audio/");
                                                fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                                fch.setMultiSelectionEnabled(false);
                                                fch.setDialogTitle("Choose play-folder:");

                                                int result = fch.showOpenDialog(BackVocalFrame.this);
                                                // Если директория выбрана, покажем ее в сообщении
                                                if (result == JFileChooser.APPROVE_OPTION ) {
                                                    System.out.println("Chousen dir: " + fch.getSelectedFile());
                                                    getSelectedItem().getPlaylist().setTracks(fch.getSelectedFile());
                                                } else {
                                                    System.out.println("Dir was not chousen...");
                                                }
                                            }
                                        });
                                    }
                                };

                                clearBindBtn = new JButton("Clear bind") {
                                    {
                                        setFont(btnsFont);
                                        setEnabled(false);
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                int req = JOptionPane.showConfirmDialog(BackVocalFrame.this, "Clear current playlist?", "Confirm:", JOptionPane.OK_OPTION);
                                                if (req == 0) {
                                                    Out.Print("Clearing the playlist " + getSelectedItem().getName());
                                                    getSelectedItem().getPlaylist().clearTracks();
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

                                nowPlayedLabel1 = new JLabel("Now played: ") {
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
                                add(nowPlayedLabel1);
                            }
                        };

                        JToolBar toolBar = new JToolBar("Still draggable") {
                            {
                                moveUpBtn = new JButton("Move it Up") {
                                    {
                                        setForeground(Color.BLUE);
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                getSelectedItem().moveSelectedUp();
                                            }
                                        });
                                    }
                                };

                                moveDownBtn = new JButton("Remove track") {
                                    {
                                        setForeground(Color.BLUE);
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                getSelectedItem().removeSelected();
                                            }
                                        });
                                    }
                                };

                                removeBtn = new JButton("Move it Down") {
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

                                add(moveUpBtn);
                                add(new JSeparator());
                                add(moveDownBtn);
                                add(new JSeparator());
                                add(removeBtn);
                            }
                        };

                        add(toolBar, BorderLayout.NORTH);
                        add(playDateScroll, BorderLayout.CENTER);
                        add(downBtnsPane, BorderLayout.SOUTH);
                    }
                };

                add(playListsScroll, BorderLayout.CENTER);
                add(downShedulePane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        addWindowListener(this);

        loadTracksDB();
        Out.Print("Show the frame...");
        pack();
        setVisible(true);
        setMinimumSize(new Dimension(dayItems[0].getWidth() * 7 + 48, 700));
        setLocationRelativeTo(null);
        playProgress.setPreferredSize(new Dimension(frame.getWidth() / 3, 30));

        try {
            Out.Print("Starting the Executor...");
            executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                while (true) {
                    for (PlayDataItem weakdayItem : getWeakdayItems()) {
                        if (weakdayItem.isPlayed()) {
                            if (!weakdayItem.inSchedulingTimeAccept()) {
                                weakdayItem.stop();
                                JOptionPane.showConfirmDialog(BackVocalFrame.this, "Timer out! Music has stopped.", "Timer out:", JOptionPane.DEFAULT_OPTION);
                            }
                        }
                    }

                    try {Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
//            executor.shutdown(); //shutdown executor
//            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
//                System.out.println("AWAIT");
//            }
        } catch (Exception e) {
            Out.Print("Executor loading exception: " + e.getMessage());
        }

        repaint();
    }

    public static void resetDownPaneSelect() {
        Component[] comps = playDatePane.getComponents();
        for (Component comp : comps) {
            if (comp instanceof PlayDataItem) {
                ((PlayDataItem) comp).setSelected(false);
            }
        }

    }

    public static void showPlayList(Playlist playlist) {
        if (playlist == null) {
            centerPlaylistsPane.removeAll();
            playListsScroll.repaint();
            playListsScroll.revalidate();
            return;
        }

        centerPlaylistsPane.removeAll();
        centerPlaylistsPane.add(new JLabel(playlist.getName() + "`s playlist:") {{
            setBorder(new EmptyBorder(3, 6, 0, 0));
            setFont(headersFontSmall);
            setForeground(Color.WHITE);
        }}, BorderLayout.NORTH);
        centerPlaylistsPane.add(playlist, BorderLayout.CENTER);

        System.out.println("Added playlist named " + playlist.getName());
        playListsScroll.repaint();
        playListsScroll.revalidate();
    }

    public static ArrayList<PlayDataItem> getWeakdayItems() {
        ArrayList<PlayDataItem> result = new ArrayList<>();

        for (Component comp : playDatePane.getComponents()) {
            if (comp instanceof PlayDataItem) {
                result.add((PlayDataItem) comp);
            }
        }

        return result;
    }

    public static void updatePlayedLabelText() {
        List<PlayDataItem> played = getSoundedItems();
        String mes = "<html>Playing: ";
        for (PlayDataItem playItem : played) {
            mes += "<b color='YELLOW'>" + playItem.getName() + ":</b> '" + playItem.getActiveTrackName() + "' ";
        }
        nowPlayedLabel1.setText(mes);
    }

    public static JFrame getFrame() {return frame;}

    public static PlayDataItem getSelectedItem() {
        Component[] comps = playDatePane.getComponents();
        for (Component comp : comps) {
            if (comp instanceof PlayDataItem) {
                if (((PlayDataItem) comp).isSelected()) {
                    return ((PlayDataItem) comp);
                }
            }
        }

        return null;
    }

    private static List<PlayDataItem> getSoundedItems() {
        List<PlayDataItem> result = new ArrayList<>();

        Component[] comps = playDatePane.getComponents();
        for (Component comp : comps) {
            if (comp instanceof PlayDataItem) {
                if (((PlayDataItem) comp).isPlayed()) {
                    result.add((PlayDataItem) comp);
                }
            }
        }

        return result;
    }

    public static void enableControls(boolean enable) {
        bindListBtn.setEnabled(enable);
        clearBindBtn.setEnabled(enable);
    }

    private void stateChanged() {
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
    }

    private static int counter = 0;
    private static void loadTracksDB() {
        Out.Print("Loading the tracks...");

        for (String day : days) {
            loadDay(day);
        }

        Out.Print("Loading tracks accomplished.");
    }

    private static void loadDay(String day) {
        Out.Print("Try to load the day '" + day + "'...");
        try {
            String meta;
            String[] data;

            try {
                // META loading:
                meta = Files.readString(Paths.get("./resources/scheduler/" + day + ".meta"), StandardCharsets.UTF_8);
                data = meta.split("NN_");

                Out.Print("Date in: " + Arrays.toString(data));
                dayItems[counter] = new PlayDataItem(
                        day,
                        data[1].split("_EE")[1],
                        data[2].split("_EE")[1],
                        data[3].split("_EE")[1],
                        Boolean.parseBoolean(data[4].split("_EE")[1]));

                // LIST loading:
                List<String> tracks = Files.lines(Paths.get("./resources/scheduler/" + day + ".list"), StandardCharsets.UTF_8).collect(Collectors.toList());

                for (String track : tracks) {
                    try {
                        dayItems[counter].addTrack(Paths.get(track));
                    } catch (Exception e) {
                        if (Files.notExists(Paths.get(track))) {
                            Out.Print("Track not exist:", Out.LEVEL.WARN, e.getStackTrace());
                        } else {
                            Out.Print("Unknown err:", Out.LEVEL.ERROR, e.getStackTrace());
                        }
                    }
                }

            } catch (IllegalArgumentException iae) {
                Out.Print("Err:", Out.LEVEL.WARN, iae.getStackTrace());
                iae.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException aibe) {
                Out.Print("Err:", Out.LEVEL.WARN, aibe.getStackTrace());
                aibe.printStackTrace();
            } catch (MalformedInputException mie) {
                Out.Print("Err:", Out.LEVEL.WARN, mie.getStackTrace());
                mie.printStackTrace();
            } catch (NoSuchFileException fnf) {
                Out.Print("PlayList for " + day + " is not exist.", Out.LEVEL.WARN);
                dayItems[counter] =
                        new PlayDataItem(
                                day,
                                "00:00:00", "23:59:59", "00:00:00",
                                true);
            } catch (Exception e) {
                Out.Print("Meta loading err:", Out.LEVEL.WARN, e.getStackTrace());
                e.printStackTrace();
            }

            try {
                playDatePane.add(dayItems[counter]);
            } catch (Exception e) {
                Out.Print("Add err:", Out.LEVEL.ERROR, e.getStackTrace());
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Out.Print("Loading global err:", Out.LEVEL.ERROR, e.getStackTrace());
        }

        Out.Print("Counter increase on 1 now.");
        counter++;
    }

    private static void saveTracksDB() {
        ArrayList<PlayDataItem> wdItems = getWeakdayItems();
        for (PlayDataItem wdItem : wdItems) {
            wdItem.saveToFile();
        }
    }


    public static void setProgress(int prog) {
        if (prog < 0) {prog = 0;} else if (prog > 100) {prog = 100;}
        playProgress.setValue(prog);
    }

    private void tray() throws AWTException {
        Out.Print("Traying the frame...");

        frame.dispose();
        tray.add(trayIcon);
        trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
    }

    private void detray() {
        Out.Print("De-Traying the frame...");
        BackVocalFrame.this.setVisible(true);
        BackVocalFrame.this.setState(JFrame.NORMAL);
        tray.remove(trayIcon);
    }


    // Listeners:
    @Override
    public void windowOpened(WindowEvent e) {
    	Out.Print("Opening the frame...");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this, "Are You sure?..", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
        if (req == 0) {
            executor.shutdownNow();
            saveTracksDB();
            BackVocalFrame.this.dispose();
            MainClass.exit(Codes.OLL_OK);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        Out.Print("Closing the frame...");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("./resources/icons/0.png");

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Test");
            defaultItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detray();
                }
            });
            popup.add(defaultItem);

            MenuItem close = new MenuItem("Close");
            close.addActionListener(e12 -> MainClass.exit(Codes.OLL_OK));
            popup.add(close);

            trayIcon = new TrayIcon(image, "BVF", popup);
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detray();
                }
            });
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("BackVocalStudio");

            try {tray();
            } catch (AWTException awtException) {
                awtException.printStackTrace();
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, Arrays.stream(awtException.getStackTrace()).toArray());
            }
        }
    }

    @Override
    public void windowDeiconified(WindowEvent e) {detray();}

    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
