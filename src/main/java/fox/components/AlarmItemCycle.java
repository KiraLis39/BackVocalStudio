package fox.components;

import java.nio.file.Path;
import java.time.LocalDateTime;


public class AlarmItemCycle implements iAlarm {
    private String time;
    private Path track;
    private LocalDateTime startTime;

    public AlarmItemCycle(String time, Path path) {
        this.time = time;
        this.track = path;
        resetStartTime();
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public Path getTrack() {
        return track;
    }

    public LocalDateTime getStartTime() {return startTime;}
    public void resetStartTime() {startTime = LocalDateTime.now();}

    @Override
    public String toString() {
        return "[" + time + "]  " + track.toFile().getName() + "";
    }

    public boolean isCycled() {return true;}
    public boolean isWasPlayed() {return false;}
    public void wasPlayed(boolean b) {}
}
