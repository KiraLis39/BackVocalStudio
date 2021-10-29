package fox.components;

import java.nio.file.Path;


public interface iAlarm {
    String getTime();
    Path getTrack();
    boolean isCycled();
    boolean isWasPlayed();
    void wasPlayed(boolean b);
}
