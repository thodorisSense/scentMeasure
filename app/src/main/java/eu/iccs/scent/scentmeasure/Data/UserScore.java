package eu.iccs.scent.scentmeasure.Data;

/**
 * Created by theodoropoulos on 14/11/2018.
 */

public class UserScore {
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    String nickname;
    String score;
}
