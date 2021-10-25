package registry;

public enum Codes {
    BAD_DB_SAVING(114),
    OLL_OK(0),
    NOT_CORRECT_SHUTDOWN(22);

    private int code;

    Codes(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
