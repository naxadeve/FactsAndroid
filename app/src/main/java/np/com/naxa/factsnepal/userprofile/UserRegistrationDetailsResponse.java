package np.com.naxa.factsnepal.userprofile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserRegistrationDetailsResponse {

    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("is_already_exist")
    @Expose
    private boolean isAlreadyExist;
    @SerializedName("data")
    @Expose
    private UserRegistrationDetails data;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("token")
    @Expose
    private String token;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UserRegistrationDetails getData() {
        return data;
    }

    public void setData(UserRegistrationDetails data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isAlreadyExist() {
        return isAlreadyExist;
    }

    public void setAlreadyExist(boolean alreadyExist) {
        isAlreadyExist = alreadyExist;
    }
}
