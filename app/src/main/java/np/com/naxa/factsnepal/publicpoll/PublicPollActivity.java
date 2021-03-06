package np.com.naxa.factsnepal.publicpoll;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import np.com.naxa.factsnepal.R;
import np.com.naxa.factsnepal.common.BaseActivity;
import np.com.naxa.factsnepal.userprofile.UserLoginResponse;
import np.com.naxa.factsnepal.utils.ActivityUtil;
import np.com.naxa.factsnepal.utils.SharedPreferenceUtils;

import static np.com.naxa.factsnepal.common.Constant.KEY_EXTRA_OBJECT;
import static np.com.naxa.factsnepal.common.Constant.KEY_OBJECT;
import static np.com.naxa.factsnepal.common.Constant.SharedPrefKey.KEY_USER_LOGGED_IN_DETAILS;

import static np.com.naxa.factsnepal.common.Constant.Network.KEY_MAX_RETRY_COUNT;

public class PublicPollActivity extends BaseActivity {
    private static final String TAG = "PublicPollActivity";

    private TextView tvQuestionTitle ;
    private int retryCounter = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_poll);
        tvQuestionTitle = findViewById(R.id.tv_question_title);
        setupToolbar();

//        setupButtonForDemo("checkbox");
        createProgressDialog("Loading Poll Data");
        fetchPublicPollFromServer();
    }

    private void setupButtonForDemo(@NonNull PublicPollQuestionDetail publicPollQuestionDetail) {
        tvQuestionTitle.setText(publicPollQuestionDetail.getQuestion());

        LinearLayout view = findViewById(R.id.layout_button_wrapper);
        View button = null;

        View.OnClickListener onClick = v -> {
            createProgressDialog("Sending data...");
            Log.d(TAG, "setupButtonForDemo: view tag " +v.getTag().toString());

            Gson gson = new Gson();
            UserLoginResponse userLoginResponse = gson.fromJson((SharedPreferenceUtils.getInstance(this).getStringValue(KEY_USER_LOGGED_IN_DETAILS, null)), UserLoginResponse.class);
            postPublicPollAnswerToServer(userLoginResponse.getUserLoginDetails().getId(), publicPollQuestionDetail, Integer.parseInt(v.getTag().toString()));
        };

        List<Option> optionList = publicPollQuestionDetail.getOptions();
        for (Option option : optionList) {
            button = LayoutInflater.from(this).inflate(R.layout.layout_button_survey, view, false);
            ((MaterialButton) button).setText(option.getQuestion());
            ((MaterialButton) button).setTag(option.getId().toString());
            button.setOnClickListener(onClick);
            view.addView(button);

        }

        hideProgressDialog();
    }



    private void fetchPublicPollFromServer(){
        apiInterface.getPublicPollQuestionDetail()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(KEY_MAX_RETRY_COUNT)
                .retryWhen(errors -> errors.flatMap(error -> Observable.timer(5, TimeUnit.SECONDS)))
                .subscribe(new DisposableObserver<List<PublicPollQuestionDetail>>() {
                    @Override
                    public void onNext(List<PublicPollQuestionDetail> publicPollQuestionDetailList) {

                        if (publicPollQuestionDetailList == null){
                            hideProgressDialog();
                            showToast("Null response from the server");
                        }else {
                            setupButtonForDemo(publicPollQuestionDetailList.get(0));
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void postPublicPollAnswerToServer (int userId, @NonNull PublicPollQuestionDetail publicPollQuestionDetail, int ansserId){

        apiInterface.postPublicPOllAnswerResponse(publicPollQuestionDetail.getId(), ansserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(5)
                .retryWhen(errors -> errors.flatMap(error -> Observable.timer(5, TimeUnit.SECONDS)))
                .subscribe(new DisposableObserver<PostPublicPollAnswerResponse>() {
                    @Override
                    public void onNext(PostPublicPollAnswerResponse postPublicPollAnswerResponse) {
                        hideProgressDialog();
                        if (postPublicPollAnswerResponse == null){
                            showToast("Null response from the server.");
                        }else {
                            if(postPublicPollAnswerResponse.getSuccess()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(KEY_OBJECT, postPublicPollAnswerResponse);
                                hashMap.put(KEY_EXTRA_OBJECT, publicPollQuestionDetail);
                                ActivityUtil.openActivity(PublicPollResultActivity.class, PublicPollActivity.this, hashMap, false);
                            }else {
                                showToast(postPublicPollAnswerResponse.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


}
