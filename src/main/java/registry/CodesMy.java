package registry;

public enum CodesMy {
    BAD_DB_SAVING(114),
    OLL_OK(0);

    private int code;

    CodesMy(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
