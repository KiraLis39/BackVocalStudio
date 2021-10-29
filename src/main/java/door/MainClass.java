package door;

import fox.out.Out;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import gui.BackVocalFrame;
import registry.Codes;

public class MainClass {
    private static Path[] importantDirs;
    private static Long startTime;
    private static Thread showLogoThread;
    private static boolean closeLogoFlag;

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();

        showLogoThread = new Thread(() -> {
            UIManager.put("DrawPadUI", "drawpad.BasicDrawPadUI");
            JFrame logo = new JFrame() {
                {
                    setPreferredSize(new Dimension(400, 300));
                    setTitle("logo");
                    setUndecorated(true);
                    getContentPane().setBackground(Color.black);

                    add(new JLabel("(здесь может быть ваша реклама или лого)") {{setHorizontalAlignment(0);setForeground(Color.WHITE);}});

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
            Out.setLogsCountAllow(5);
            Out.setErrorLevel(Out.LEVEL.ACCENT);

            checkImportantDirectoriesExists();
        } catch (Exception e) {
            Out.Print(MainClass.class, Out.LEVEL.ERROR, "Has error in main: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null,
                    "Что-то пошло не так при\nинициализации программы:\n" + e.getMessage(), "Ошибка!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            System.exit(Codes.START_FAILED.code());
        }

        try {
            Out.Print(MainClass.class, Out.LEVEL.INFO, "Start!");
            loadUIM();
            DayCore.loadDays();
        } catch (Exception e) {
            Out.Print(MainClass.class, Out.LEVEL.ERROR, "Has error in main: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null,
                    "Что-то пошло не так при\nзапуске программы:\n" + e.getMessage(), "Ошибка!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            System.exit(Codes.START_FAILED.code());
        }

        Out.Print(BackVocalFrame.class, Out.LEVEL.INFO, "Build the frame...");
        new BackVocalFrame();
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
                Paths.get("./resources/audio/sound"),
                Paths.get("./resources/scheduler/"),
                Paths.get("./resources/icons/")
        };

        for (Path importantDir : importantDirs) {
            if (Files.notExists(importantDir)) {
                Files.createDirectories(importantDir);
            }
        }

    }

    public static void exit(Codes code) {
        Out.Print(MainClass.class, Out.LEVEL.DEBUG, "Finish with code: " + code);
        System.exit(code.code());
    }

    public static Long getStartTime() {
        return startTime;
    }

    public static void closeLogo() {
        if (showLogoThread != null && showLogoThread.isAlive()) {
            closeLogoFlag = true;
        }
    }
}
