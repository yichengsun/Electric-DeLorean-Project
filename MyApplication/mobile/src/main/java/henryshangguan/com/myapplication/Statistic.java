package henryshangguan.com.myapplication;

import java.util.UUID;

/**
 * Created by henryshangguan on 6/18/15.
 */
public class Statistic {
    private String title;
    private int number;

    public Statistic(String title) {
        this.title = title;
    }

    public UUID getId() {
        return UUID.randomUUID();
    }

    public String getTitle() {
        return title;
    }


}
