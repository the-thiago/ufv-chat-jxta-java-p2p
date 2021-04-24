package ufv.chat.jxta.p2p;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util {

    public static String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
        Date hour = Calendar.getInstance().getTime();
        return sdf.format(hour);
    }

}
