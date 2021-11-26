package door;

import fox.out.Out;


public class Exit {

    private Exit() {}

    public static void exit(int code, String message) {
        Out.Print(MainClass.class, Out.LEVEL.DEBUG, "Finish with code: " + code);
        if (message != null) {
            ErrorSender.send("Exit code #" + code + ">> " + message);
        }

        if (code != 0) {
            throw new RuntimeException(message);
        }

        System.exit(code);
    }
}
