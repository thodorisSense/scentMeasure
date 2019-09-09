package eu.iccs.scent.scentmeasure.Data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by theodoropoulos on 28/6/2018.
 */
@Dao
public interface UserOptionsDao {

    @Query("SELECT * FROM userOptions")
    List<UserOptions> getAll();

    @Query("SELECT * FROM userOptions WHERE user_name LIKE :userName LIMIT 1")
    UserOptions findByName(String userName);

    @Insert
    void insertAll( UserOptions... userOptions);

    @Delete
    void delete(UserOptions userOptions);

}
