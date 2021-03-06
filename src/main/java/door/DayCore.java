package door;

import fox.components.AlarmItem;
import fox.components.iAlarm;
import fox.out.Out;
import fox.utils.FOptionPane;
import gui.BackVocalFrame;
import gui.PlayDataItem;
import javax.swing.*;
import java.awt.*;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DayCore {
    private static final String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final PlayDataItem[] dayItems = new PlayDataItem[7];
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static ExecutorService executor = Executors.newFixedThreadPool(2);
    private static short playSleepTime = 1500, alarmSleepTime = 1000;

    public static void loadDays() {
        Out.Print(DayCore.class, Out.LEVEL.DEBUG, "Loading tracks...");
        int daysCounter = 0;

        for (String day : days) {
            Out.Print(DayCore.class, Out.LEVEL.DEBUG, "Try to load the day '" + day + "'...");
            loadDay(day, daysCounter);
            daysCounter++;
        }

        Out.Print(DayCore.class, Out.LEVEL.DEBUG, "Loading tracks accomplished.");
    }

    public static void loadDay(String dayName, int dayIndex) {
        try {
            // META loading:
            String meta = Files.readString(Paths.get("./resources/scheduler/" + dayName + ".meta"), StandardCharsets.UTF_8);
            String[] data = meta.split("NN_");

            dayItems[dayIndex] = new PlayDataItem(
                    dayName,
                    data[1].split("_EE")[1],
                    data[2].split("_EE")[1],
                    Boolean.parseBoolean(data[3].split("_EE")[1]));


            // ALARMS loading:
            java.util.List<String> alarms = Files.lines(Paths.get("./resources/scheduler/" + dayName + ".alarms"), StandardCharsets.UTF_8).collect(Collectors.toList());
            for (String alarm : alarms) {
                try {
                    String time = alarm.split(">")[0];
                    Path track = Paths.get(alarm.split(">")[1]);
                    Boolean isCycled = false;
                    try {isCycled = Boolean.parseBoolean(alarm.split(">")[2]);
                    } catch (Exception e) {/* IGNORE OLD PLAYLIST MODEL */}

                    if (Files.notExists(track)) {
                        Out.Print(DayCore.class, Out.LEVEL.WARN, "Alarm file not exist:");
                    } else {
                        if (time.length() == 8 || time.startsWith("R")) {
                            dayItems[dayIndex].addAlarm(time, track, isCycled);
                        } else {
                            Out.Print(DayCore.class, Out.LEVEL.WARN, "Time is not correct: " + time);
                        }
                    }
                } catch (Exception e) {
                    Out.Print(DayCore.class, Out.LEVEL.ERROR, "Alarms loading exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }


            // LIST loading:
            java.util.List<String> trackss = Files.lines(Paths.get("./resources/scheduler/" + dayName + ".list"), StandardCharsets.UTF_8).collect(Collectors.toList());
            List<Path> tracks = new ArrayList<>();
            for (String s : trackss) {
                tracks.add(Paths.get(s));
            }
            dayItems[dayIndex].addTracks(tracks);
        } catch (IllegalArgumentException iae) {
            Out.Print(DayCore.class, Out.LEVEL.ERROR, iae.getMessage());
            iae.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException aibe) {
            Out.Print(DayCore.class, Out.LEVEL.ERROR, aibe.getMessage());
            aibe.printStackTrace();
        } catch (MalformedInputException mie) {
            Out.Print(DayCore.class, Out.LEVEL.ERROR, mie.getMessage());
            mie.printStackTrace();
        } catch (NoSuchFileException fnf) {
            Out.Print(DayCore.class, Out.LEVEL.ERROR, "PlayList for " + dayName + " is not exist.");
            dayItems[dayIndex] =
                    new PlayDataItem(
                            dayName,
                            "12:00:00", "12:00:00",
                            true);
        } catch (Exception e) {
            Out.Print(DayCore.class, Out.LEVEL.ERROR, "Meta loading err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static PlayDataItem getDayItem(int i) {
        return dayItems[i];
    }

    public static String[] getDays() {
        return days;
    }

    public static void execute() {
        Out.Print(DayCore.class, Out.LEVEL.DEBUG, "Starting the Executors...");

        try {
            executor.execute(run01());
            executor.execute(run02());
            Out.Print(DayCore.class, Out.LEVEL.ACCENT, "== Launch time is: <" +
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - MainClass.getStartTime().get()) + " sec.> =="
            );
        } catch (Exception e) {
            Out.Print(DayCore.class, Out.LEVEL.WARN, "Executors loading exception: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null,
                    "???????????? ?????? ????????????\n????????????????????????:\n" + e.getMessage(), "????????????!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Runnable run01() {
        return () -> {
            Out.Print(DayCore.class, Out.LEVEL.ACCENT, "Play executor started.");

            try {Thread.sleep(250);
            } catch (InterruptedException e) {/* IGNORE START PAUSE */}

            String today = LocalDateTime.now().getDayOfWeek().name();

            while(!executor.isShutdown()) {
                try {
                    for (PlayDataItem weakdayItem : BackVocalFrame.getWeekdayItems()) {
                        if (!today.equalsIgnoreCase(LocalDateTime.now().getDayOfWeek().name())) {
                            today = LocalDateTime.now().getDayOfWeek().name();
                            weakdayItem.setHandStopped(false);
                        }


                        if (!weakdayItem.getName().equalsIgnoreCase(today)) {
                            continue;
                        }

                        if (weakdayItem.isAlarmPlayed()) {
                            BackVocalFrame.getFrame().repaint();
                        }

                        if (!weakdayItem.inSchedulingTimeAccept()) {
                            if (weakdayItem.isPlayed()) {
                                weakdayItem.stop();
                                weakdayItem.repaint();
                                new FOptionPane("?????????? ??????????:",
                                        "?????????? ??????????!\n?????????????????????????????? '" + weakdayItem.getName() + "' ??????????????????????.",
                                        FOptionPane.TYPE.DEFAULT, null);
                            }
                        } else {
                            if (weakdayItem.getPlayPane().isEmpty()) {
                                continue;
                            }

                            if (!weakdayItem.isPlayed() && !weakdayItem.isHandStopped() && !weakdayItem.isAlarmPlayed()) {
                                weakdayItem.play(-1);
                                weakdayItem.setSelected(true);
                            }
                        }

                    }
                } catch (Exception e) {
                    Out.Print(DayCore.class, Out.LEVEL.WARN, "Exception into play executor: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showConfirmDialog(null,
                            "???????????? ?????? ????????????\n?????????????????????? ??????????????????????????????:\n" + e.getMessage(), "????????????!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }

                try {
                    Thread.sleep(playSleepTime);
                } catch (InterruptedException e) {
                    Out.Print(DayCore.class, Out.LEVEL.WARN, "Play executor was interrupted incorrectly: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            Out.Print(DayCore.class, Out.LEVEL.ACCENT, "Play executor ended.");
        };
    }

    private static Runnable run02() {
        return () -> {
            try {Thread.sleep(150);
            } catch (InterruptedException e) {/* IGNORE START PAUSE */}

            while(!executor.isShutdown()) {
                BackVocalFrame.setCurrentTimeText("<html>Now: <b color='YELLOW'>" + sdf.format(System.currentTimeMillis()) + "</b></html>");

                try {
                    iAlarm nextAlarm = null;

                    for (PlayDataItem weekdayItem : BackVocalFrame.getWeekdayItems()) {
                        if (!weekdayItem.getName().equalsIgnoreCase(LocalDateTime.now().getDayOfWeek().name())) {continue;}
                        if (!weekdayItem.inSchedulingTimeAccept()) {continue;}

                        for (iAlarm s : weekdayItem.getAlarmData()) {
                            if (s.isWasPlayed()) {continue;}
                            if (weekdayItem.isTimeCome(s)) {
                                nextAlarm = s;
                                break;
                            }
                        }

                        if (nextAlarm != null) {
                            weekdayItem.stop();
                            weekdayItem.playAlarm(nextAlarm.getTrack());
                            nextAlarm.wasPlayed(true);
                            break;
                        }
                    }

                } catch (Exception e) {
                    Out.Print(DayCore.class, Out.LEVEL.WARN, "Exception into alarms executor: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showConfirmDialog(null,
                            "???????????? ?????? ????????????\n?????????????????????? ????????????????????:\n" + e.getMessage(), "????????????!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }

                try {
                    Thread.sleep(alarmSleepTime);
                } catch (InterruptedException e) {
                    Out.Print(DayCore.class, Out.LEVEL.WARN, "Alarms executor was interrupted incorrectly: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            Out.Print(DayCore.class, Out.LEVEL.ACCENT, "Alarms executor ended.");
        };
    }

    public static String getFormatted(Long time) {
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(time);
    }

    public static void shutdown() {
        if (executor == null) {return;}

        Out.Print(PlayDataItem.class, Out.LEVEL.ACCENT, "Executors shutting down...");
        executor.shutdown(); //shutdown executor

        try {
            int maxCycleStopAwait = 3;
            while (!executor.awaitTermination(3, TimeUnit.SECONDS) && maxCycleStopAwait > 0) {
                maxCycleStopAwait--;
                Out.Print(PlayDataItem.class, Out.LEVEL.WARN, "Waiting for a stop takes more time than seems...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!executor.isTerminated()) {
            Out.Print(PlayDataItem.class, Out.LEVEL.ERROR, "Executors can`t stopped, than was killed! It`s bad.");
            executor.shutdownNow();
        }
    }

    public static boolean isShutdowned() {
        if (executor == null) {return true;}
        return executor.isTerminated();
    }
}
