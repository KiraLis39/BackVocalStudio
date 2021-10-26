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
    private static JDialog logo;

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();

        Thread showLogoThread = new Thread(() -> {
            UIManager.put("DrawPadUI", "drawpad.BasicDrawPadUI");
            logo = new JDialog() {
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

            while (!logo.isVisible()) {

            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logo.dispose();
        });
        showLogoThread.start();

        try {
            Out.setEnabled(true);
            Out.setLogsCountAllow(5);

            checkImportantDirectoriesExists();

            Out.Print(MainClass.class, Out.LEVEL.INFO, "Start!");
            Out.setErrorLevel(Out.LEVEL.ACCENT);

            loadUIM();

            DayCore.loadDays();

            showLogoThread.join();
            new BackVocalFrame();

        } catch (Exception e) {
            Out.Print(MainClass.class, Out.LEVEL.ERROR, "Has error in main: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null,
                    "Что-то пошло не так при\nзапуске программы:\n" + e.getMessage(), "Ошибка!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loadUIM() {
        Out.Print(MainClass.class, Out.LEVEL.DEBUG, "Set the UIManagers view.");

        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
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
}
