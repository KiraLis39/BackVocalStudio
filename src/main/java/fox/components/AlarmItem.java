package fox.components;

import java.nio.file.Path;


public class AlarmItem implements iAlarm {
    private String time;
    private Path track;
    private boolean wasPlayed = false;

    public AlarmItem(String time, Path path) {
        this.time = time;
        this.track = path;
    }

    public String getTime() {return time;}

    public Path getTrack() {return track;}

    @Override
    public boolean isCycled() {return false;}

    @Override
    public String toString() {
        return "(" + time + ")  " + track.toFile().getName() + "";
    }

    public void wasPlayed(boolean wasPlayed) {
        this.wasPlayed = wasPlayed;
    }
    public boolean isWasPlayed() {return this.wasPlayed;}
}
