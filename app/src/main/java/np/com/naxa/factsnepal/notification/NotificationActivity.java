package np.com.naxa.factsnepal.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import np.com.naxa.factsnepal.R;
import np.com.naxa.factsnepal.common.BaseActivity;
import np.com.naxa.factsnepal.common.BaseRecyclerViewAdapter;
import np.com.naxa.factsnepal.feed.list.FeedListActivity;
import np.com.naxa.factsnepal.utils.SharedPreferenceUtils;

public class NotificationActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private BaseRecyclerViewAdapter<FactsNotification, FactsNotificationVH> adapter;
    private static final String TAG = "NotificationActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        setupToolbar();
        bindUI();

        Gson gson = new Gson();
        SharedPreferenceUtils sharedPreferenceUtils = new SharedPreferenceUtils(this);
        ArrayList<FactsNotification> factsNotification = gson.fromJson(sharedPreferenceUtils.getStringValue(NotificationCount.KEY_NOTIFICATION_LIST, "")
                , new TypeToken<List<FactsNotification>>(){}.getType());
        setupListAdapter(factsNotification);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(NotificationActivity.this, FeedListActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void bindUI() {
        recyclerView = findViewById(R.id.rv_notification);
    }


    private void setupListAdapter(List<FactsNotification> factsNotifications) {

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new BaseRecyclerViewAdapter<FactsNotification, FactsNotificationVH>(factsNotifications, R.layout.item_notification) {


            @Override
            public void viewBinded(FactsNotificationVH factsNotificationVH, FactsNotification factsNotification, int position) {
                factsNotificationVH.bindView(factsNotification);
                factsNotificationVH.itemView.setOnClickListener((v -> {

                    updateSeenStatus(position, factsNotification);
                }));
            }


            @Override
            public FactsNotificationVH attachViewHolder(View view) {
                return new FactsNotificationVH(view);
            }
        };

        recyclerView.setAdapter(adapter);
    }


    private void updateSeenStatus(int position, @NonNull FactsNotification factsNotification) {
//        factsNotification.setRead(true);
//        Gson gson = new Gson();
//        String josnSeenFactNotification = gson.toJson(factsNotification);
        Log.d(TAG, "viewBinded: clicked : "+factsNotification.isRead());

        SharedPreferenceUtils sharedPreferenceUtils = new SharedPreferenceUtils(this);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(sharedPreferenceUtils.getStringValue(NotificationCount.KEY_NOTIFICATION_LIST, ""));
            jsonArray.getJSONObject(position).remove("idRead");
            jsonArray.getJSONObject(position).put("isRead", true);

            sharedPreferenceUtils.setValue(NotificationCount.KEY_NOTIFICATION_LIST, jsonArray.toString());

            Log.d(TAG, "viewBinded: clicked last: "+jsonArray.getJSONObject(position).getString("isRead"));

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }


}
