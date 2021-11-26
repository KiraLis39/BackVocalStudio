package door;

import fox.out.Out;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import gui.BackVocalFrame;
import registry.Codes;

public class MainClass {
    private static Path[] importantDirs;
    private static AtomicLong startTime;
    private static Thread showLogoThread;
    private static boolean closeLogoFlag;
    private static BufferedImage logoImage;

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        startTime = new AtomicLong(System.currentTimeMillis());

        try {logoImage = ImageIO.read(new File("./logo.png"));
        } catch (Exception e) {/* IGNORE LOGO LOAD */}

        showLogoThread = new Thread(() -> {
            UIManager.put("DrawPadUI", "drawpad.BasicDrawPadUI");
            JFrame logo = new JFrame() {
                @Override
                public void paint(Graphics g) {
                    super.paint(g);
                    if (logoImage != null) {
                        g.drawImage(logoImage,
                                0,0,
                                400, 300,

                                0,0,
                                logoImage.getWidth(), logoImage.getHeight(),
                                null);
                        g.dispose();
                    }
                }

                {
                    setPreferredSize(new Dimension(400, 300));
                    setTitle("logo");
                    setUndecorated(true);
                    getContentPane().setBackground(Color.black);

                    if (logoImage == null) {
                        add(new JLabel("<html><p align='center'>(здесь может быть ваша реклама или лого)<hr>" +
                                "<p align='center'>Положите <b>logo.png</b> в папку с программой") {{setHorizontalAlignment(0);setForeground(Color.WHITE);}});
                    } else {
                        setBackground(new Color(0,0,0,0));
                    }

                    pack();
                    setLocationRelativeTo(null);
                    setVisible(true);
                }
            };

            try {
                while (!closeLogoFlag) {
                    Thread.sleep(250);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                logo.dispose();
                Thread.currentThread().interrupt();
            }
        });
        showLogoThread.start();

        try {
            Out.setEnabled(true);
            Out.setLogsCountAllow(10);
            Out.setErrorLevel(Out.LEVEL.ACCENT);

            checkImportantDirectoriesExists();
        } catch (Exception e) {
            Out.Print(MainClass.class, Out.LEVEL.ERROR, "Has error in main: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null,
                    "Что-то пошло не так при\nинициализации программы:\n" + e.getMessage(), "Ошибка!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            Exit.exit(Codes.START_FAILED.code(), "Что-то пошло не так при инициализации программы: " + e.getMessage());
        }

        try {
            Out.Print(MainClass.class, Out.LEVEL.INFO, "Start!");
            DayCore.loadDays();
        } catch (Exception e) {
            Out.Print(MainClass.class, Out.LEVEL.ERROR, "Has error in main: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null,
                    "Что-то пошло не так при\nзапуске программы:\n" + e.getMessage(), "Ошибка!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            Exit.exit(Codes.START_FAILED.code(), "Что-то пошло не так при инициализации программы: " + e.getMessage());
        }

        Out.Print(BackVocalFrame.class, Out.LEVEL.INFO, "Build the frame...");
        try {
            loadUIM();
            new BackVocalFrame();
        } catch (Exception e) {
            e.printStackTrace();
            Out.Print(MainClass.class, Out.LEVEL.ERROR, "Mail about this error sent to admin: " + e.getMessage());
            Exit.exit(Codes.RUNTIME_ERR.code(), "Runtime exception: " + e.getMessage()); // #119
        }
    }

    private static void loadUIM() {
        Out.Print(MainClass.class, Out.LEVEL.DEBUG, "Set the UIManagers view.");

        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
                e2.printStackTrace();
                Out.Print(MainClass.class, Out.LEVEL.WARN, "Has a some problem with a loading UI manager: " + e2.getMessage());
            }
        }
    }

    private static void checkImportantDirectoriesExists() throws IOException {
        Out.Print(MainClass.class, Out.LEVEL.DEBUG, "Check the important directories...");
        importantDirs = new Path[] {
                Paths.get("./resources/audio/music"),
                Paths.get("./resources/audio/alarms"),
                Paths.get("./resources/scheduler/"),
                Paths.get("./resources/icons/")
        };

        for (Path importantDir : importantDirs) {
            if (Files.notExists(importantDir)) {
                Files.createDirectories(importantDir);
            }
        }

    }

    public static AtomicLong getStartTime() {
        return startTime;
    }

    public static void closeLogo() {
        if (showLogoThread != null && showLogoThread.isAlive()) {
            closeLogoFlag = true;
        }
    }
}
