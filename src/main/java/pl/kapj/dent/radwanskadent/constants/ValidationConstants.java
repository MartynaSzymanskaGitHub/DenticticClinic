package pl.kapj.dent.radwanskadent.constants;

public final class ValidationConstants {

    private ValidationConstants() {
    }

    public static final int FIRST_NAME_MIN = 3;
    public static final int LAST_NAME_MIN = 2;
    public static final int EMAIL_MIN = 6;
    public static final int PASSWORD_MIN = 10;
    public static final int PASSWORD_MAX = 20;
    public static final int PHONE_MAX = 12;

    public static final int ROOM_NAME_MIN = 1;
    public static final int ROOM_NAME_MAX = 150;
    public static final int ROOM_DESC_MIN = 5;
    public static final int ROOM_DESC_MAX = 150;

    public static final int TREATMENT_NAME_MIN = 3;
    public static final int TREATMENT_NAME_MAX = 100;
    public static final int TREATMENT_DESC_MIN = 3;
    public static final int TREATMENT_DESC_MAX = 255;
    public static final long TREATMENT_DURATION_MIN = 1L;
}
