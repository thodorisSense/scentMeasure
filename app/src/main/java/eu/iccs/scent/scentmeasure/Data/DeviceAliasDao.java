package eu.iccs.scent.scentmeasure.Data;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;


@Dao
public interface DeviceAliasDao {
    @Query("SELECT * FROM deviceName")
    List<DeviceName> getAll();

    @Query("SELECT * FROM deviceName WHERE device_id LIKE :deviceId AND user_name LIKE :userName LIMIT 1")
    DeviceName findByName(String deviceId, String userName);

    @Insert
    void insertAll( DeviceName... devices);

    @Delete
    void delete(DeviceName device);
}