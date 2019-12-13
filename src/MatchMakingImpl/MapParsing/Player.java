
package MatchMakingImpl.MapParsing;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Player {

    @SerializedName("headPosition")
    @Expose
    private HeadPosition headPosition;
    @SerializedName("lookDirection")
    @Expose
    private String lookDirection;

    public HeadPosition getHeadPosition() {
        return headPosition;
    }

    public void setHeadPosition(HeadPosition headPosition) {
        this.headPosition = headPosition;
    }

    public String getLookDirection() {
        return lookDirection;
    }

    public void setLookDirection(String lookDirection) {
        this.lookDirection = lookDirection;
    }

}
