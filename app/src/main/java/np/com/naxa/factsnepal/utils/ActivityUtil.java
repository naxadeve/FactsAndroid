package np.com.naxa.factsnepal.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

public class ActivityUtil {

    public static void openActivity(Class className, Context context, HashMap<String, ?> data, boolean skipAnimation) {
        Intent intent = new Intent(context, className);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (skipAnimation) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }

        if (EmptyUtil.isNotNull(data) && EmptyUtil.isNotEmpty(data)) {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("map", data);
//            intent.putExtra("bundle", bundle);
            intent.putExtra("map", data);
        }

        context.startActivity(intent);
    }

    public static void openActivity(Class className, Context context) {
        openActivity(className, context, null, false);
    }
}
