package eu.iccs.scent.scentmeasure.Data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by theodoropoulos on 15/6/2018.
 */

@Database(entities = {DeviceName.class, UserOptions.class},  version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceAliasDao deviceAliasDao();
    public abstract UserOptionsDao userOptionsDao();
    private static AppDatabase INSTANCE;

 public  static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "devices_database")
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}