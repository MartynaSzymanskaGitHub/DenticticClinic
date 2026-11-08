package pl.kapj.dent.radwanskadent.constants;

import java.time.format.DateTimeFormatter;

public final class DateTimeFormats {

    private DateTimeFormats() {
    }

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    public static final DateTimeFormatter INVOICE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
}
