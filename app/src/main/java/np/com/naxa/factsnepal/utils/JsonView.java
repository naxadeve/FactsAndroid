package np.com.naxa.factsnepal.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import np.com.naxa.factsnepal.common.Constant;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * TODO: create the ratingbar view, check style adding
 * [2019-04-02] first created
 */

public class JsonView extends LinearLayout {
    public interface RenderingCompleteListener {
        void onRendered(boolean isSuccess);
    }

    enum Type {
        CHECKBOX, RADIO, RATING, EDITTEXT, SPINNER
    }

    public static class ViewParams {
        String title;
        JSONArray options;
        Type type;
        String questionID;
        int titleStyleRes = 0;
        int optionStyleRes = 0;

        static Map<String, Type> typeMap = new HashMap<String, Type>() {{
            put(Constant.ViewTypeStringKey.KEY_CHECKBOX, Type.CHECKBOX);
            put(Constant.ViewTypeStringKey.KEY_RADIO, Type.RADIO);
            put(Constant.ViewTypeStringKey.KEY_RATING_BAR, Type.RATING);
            put(Constant.ViewTypeStringKey.KEY_EDITTEXT, Type.EDITTEXT);
            put(Constant.ViewTypeStringKey.KEY_SPINNER, Type.SPINNER);
        }};

        private ViewParams(String title, JSONArray options, Type type, String questionID) {
            this.title = title;
            this.options = options;
            this.type = type;
            this.questionID = questionID;
        }

        public void addTitleStyle(int titleStyleId) {
            this.titleStyleRes = titleStyleId;
        }

        public void addOptionStyle(int optionStyleRes) {
            this.optionStyleRes = optionStyleRes;
        }

        public void addStyle(int titleStyleId, int optionStyleRes) {
            this.titleStyleRes = titleStyleId;
            this.optionStyleRes = optionStyleRes;
        }


        public static ViewParams instancefromString(String viewData) throws JSONException {
            try {
                return instanceFromJSON(new JSONObject(viewData));
            } catch (Exception e) {
                e.printStackTrace();
                throw new JSONException("Cannot render the view json \n view data : " + viewData);
            }
        }

        public static ViewParams instanceFromJSON(JSONObject jsonObject) throws JSONException {
            boolean isValidJSON = jsonObject.has("question")
                    && jsonObject.has("question_type")
                    && jsonObject.has("options");
            if (!isValidJSON) throw new JSONException("Invlaid json");

            String title = jsonObject.optString("question");
            String questionID = jsonObject.optInt("id") + "";
            String question_type = jsonObject.optString("question_type");
            JSONArray options = jsonObject.optJSONArray("options");
            return new ViewParams(title, options, typeMap.get(question_type), questionID);
        }
    }

    ViewParams params;
    Handler handler = null;
    BuildUi buildUi = null;
    RenderingCompleteListener listener = null;

    public JsonView(Context context) {
        super(context);
    }

    public JsonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JsonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public JsonView init(ViewParams params) {
        this.params = params;
        this.setOrientation(VERTICAL);
        return this;
    }

    public JsonView init(ViewParams params, RenderingCompleteListener listener) {
        this.params = params;
        this.listener = listener;
        this.setOrientation(VERTICAL);
        return this;
    }

    @SuppressLint("HandlerLeak")
    public void create() {
        if (params != null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (listener != null) {
                        listener.onRendered(msg.what == 1);
                    }
                }
            };
            buildUi = new BuildUi();
            handler.post(buildUi);
        }
    }

    private synchronized LinearLayout getCheckBoxView() {
        LinearLayout chkGroup = new LinearLayout(getContext());

        chkGroup.setOrientation(VERTICAL);
        for (int i = 0; i < params.options.length(); i++) {
            JSONObject option = params.options.optJSONObject(i);
            CheckBox checkBox;
            if (params.optionStyleRes != 0) {
                checkBox = new CheckBox(getContext(), null, params.optionStyleRes);
            } else {
                checkBox = new CheckBox(getContext());
            }
            checkBox.setText(option.optString("question"));
//            checkBox.setTag(params.questionID);
            checkBox.setTag(option.optInt("id"));
            chkGroup.addView(checkBox, i);

            int viewId = Integer.parseInt(params.questionID + option.optString("id"));
            checkBox.setId(viewId);
        }
        int viewId = ViewUtils.generateViewId();
        int checkID = Integer.parseInt(params.questionID);
        chkGroup.setId(viewId);
        chkGroup.setTag(params.questionID);
        Constant.generatedViewIdsList.add(viewId);

        return chkGroup;
    }

    private synchronized RadioGroup getRadioGroupView() {
        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setOrientation(VERTICAL);
        for (int i = 0; i < params.options.length(); i++) {
            JSONObject option = params.options.optJSONObject(i);
            RadioButton radioButton;
            if (params.optionStyleRes != 0) {
                radioButton = new RadioButton(getContext(), null, params.optionStyleRes);
            } else {
                radioButton = new RadioButton(getContext());
            }
            radioButton.setText(option.optString("question"));
            radioGroup.addView(radioButton);
//            radioButton.setTag(params.questionID);
            radioButton.setTag(option.optInt("id"));
        }
        radioGroup.setTag(params.questionID);
        int viewId = ViewUtils.generateViewId();
        int viewTagId = Integer.parseInt(params.questionID);
        radioGroup.setId(viewId);
        radioGroup.setTag(viewTagId);
        Constant.generatedViewIdsList.add(viewId);
        return radioGroup;
    }

    private synchronized Spinner getSpinnerView(){
        ArrayList<String> spinnerArray = new ArrayList<String>();

        for (int i = 0; i < params.options.length(); i++) {
            JSONObject option = params.options.optJSONObject(i);
            spinnerArray.add(option.optString("question"));
        }

        Spinner spinner = new Spinner(getContext());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);

        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setTag(params.questionID);
        int viewId = ViewUtils.generateViewId();
        spinner.setId(viewId);
        Constant.generatedViewIdsList.add(viewId);

        return spinner;
    }

    private synchronized TextView getTextView(String text, String questionID) {
        TextView textView;

        Log.d("getText", "titleRes = " + params.titleStyleRes);
        if (params.titleStyleRes != 0) {
            textView = new TextView(getContext(), null, params.titleStyleRes);
        } else {
            textView = new TextView(getContext());
        }
//        textView.setTextAppearance(params.titleStyleRes);
        textView.setTag(questionID);

        textView.setText("\t" + Html.fromHtml(text));
        textView.setTextColor(Color.parseColor("#212121"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

//        int viewId = ViewUtils.generateViewId();
//        textView.setId(viewId);
//        Constant.generatedViewIdsList.add(viewId);
        return textView;
    }

    private synchronized LinearLayout getEditTextView() {
        LinearLayout ll = new LinearLayout(getApplicationContext());
        EditText editText;
        Log.d("getText", "titleRes = " + params.titleStyleRes);
        if (params.titleStyleRes != 0) {
            editText = new EditText(getContext(), null, params.titleStyleRes);
        } else {
            editText = new EditText(getContext());
        }
        for (int i = 0; i < params.options.length(); i++) {
            JSONObject option = params.options.optJSONObject(i);

            editText.setHint(Html.fromHtml(option.optString("question")));
//            editText.setTag(params.questionID);
            editText.setTag(option.optString("id"));
            editText.setTextColor(Color.parseColor("#212121"));
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

//            int viewId = ViewUtils.generateViewId();
            int viewId = Integer.parseInt(option.optString("question_id"));
            editText.setId(viewId);
            Constant.generatedViewIdsList.add(viewId);
        }
        ll.addView(editText);


        return ll;
    }

    private synchronized LinearLayout getRatingBar() {
        // first create a layout
        LinearLayout ll = new LinearLayout(getApplicationContext());
        for (int i = 0; i < params.options.length(); i++) {

            JSONObject option = params.options.optJSONObject(i);


// now you will have to set width and height
            ll.setMinimumWidth(300);
            ll.setMinimumHeight(100);
// now init Rating bar
            RatingBar ratingBar = new RatingBar(getApplicationContext());
// now set num of stars
            ratingBar.setNumStars(Integer.parseInt(option.optString("question")));

        ratingBar.setTag(params.questionID);
//            ratingBar.setTag(option.optString("id"));
            int viewId = ViewUtils.generateViewId();
//            int viewId = Integer.parseInt(option.optString("question_id"));
            ratingBar.setId(viewId);
            Constant.generatedViewIdsList.add(viewId);
// adding ratingBar into the created layout
            ll.addView(ratingBar);
        }

        return ll;
    }

    public void destroy() {
        if (handler != null && buildUi != null) {
            handler.removeCallbacks(buildUi);
        }
    }


    class BuildUi implements Runnable {
        @Override
        public void run() {
            try {
                if (!TextUtils.isEmpty(params.title)) {
                    JsonView.this.removeAllViews();
                    JsonView.this.addView(getTextView(params.title, params.questionID), 0);
                }
                if (params.options != null && params.options.length() > 0) {
                    ViewGroup optionGroup = null;
                    switch (params.type) {
                        case CHECKBOX:
                            Log.d("JsOnView", "in checkbox");
                            optionGroup = getCheckBoxView();
                            break;
                        case RADIO:
                            optionGroup = getRadioGroupView();
                            break;
                        case RATING:
                            optionGroup = getRatingBar();
                            break;
                        case EDITTEXT:
                            optionGroup = getEditTextView();
                            break;
                        case SPINNER:
                            optionGroup = getSpinnerView();
                            break;
                    }
                    if (optionGroup != null) {
                        JsonView.this.addView(optionGroup, 1);
                        sendMessage(true);
                    } else {
                        sendMessage(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(false);
            }
        }

        private void sendMessage(boolean status) {
            Message message = new Message();
            message.what = status ? 0 : 1;
            handler.sendMessage(message);
        }
    }
}
