
package np.com.naxa.factsnepal.publicpoll;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PostPublicPollAnswerResponse implements Parcelable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("result_data")
    @Expose
    private List<ResultData> resultData = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResultData> getResultData() {
        return resultData;
    }

    public void setResultData(List<ResultData> resultData) {
        this.resultData = resultData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.success);
        dest.writeString(this.message);
        dest.writeList(this.resultData);
    }

    public PostPublicPollAnswerResponse() {
    }

    protected PostPublicPollAnswerResponse(Parcel in) {
        this.success = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.message = in.readString();
        this.resultData = new ArrayList<ResultData>();
        in.readList(this.resultData, ResultData.class.getClassLoader());
    }

    public static final Parcelable.Creator<PostPublicPollAnswerResponse> CREATOR = new Parcelable.Creator<PostPublicPollAnswerResponse>() {
        @Override
        public PostPublicPollAnswerResponse createFromParcel(Parcel source) {
            return new PostPublicPollAnswerResponse(source);
        }

        @Override
        public PostPublicPollAnswerResponse[] newArray(int size) {
            return new PostPublicPollAnswerResponse[size];
        }
    };
}
