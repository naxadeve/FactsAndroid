package np.com.naxa.factsnepal.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import np.com.naxa.factsnepal.R;
import np.com.naxa.factsnepal.feed.list.FeedListActivity;
import np.com.naxa.factsnepal.network.NetworkApiClient;
import np.com.naxa.factsnepal.network.NetworkApiInterface;
import np.com.naxa.factsnepal.surveys.surveyforms.SurveyQuestionDetailsResponse;
import np.com.naxa.factsnepal.userprofile.LoginActivity;
import np.com.naxa.factsnepal.userprofile.UpdateProfileActivity;
import np.com.naxa.factsnepal.utils.ActivityUtil;
import np.com.naxa.factsnepal.utils.SharedPreferenceUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                SharedPreferenceUtils sharedPreferenceUtils = new SharedPreferenceUtils(SplashScreenActivity.this);
                if (sharedPreferenceUtils.getBoolanValue(LoginActivity.KEY_IS_USER_LOGGED_IN, false)) {
                    ActivityUtil.openActivity(FeedListActivity.class, SplashScreenActivity.this);
                } else {
                    ActivityUtil.openActivity(LoginActivity.class, SplashScreenActivity.this, null, false);
                }
            }
        }, 2000);
    }

}
