package se.infomaker.iap.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import se.infomaker.frt.moduleinterface.action.GlobalActionHandler;
import se.infomaker.iap.action.Operation;
import se.infomaker.iap.theme.view.ThemeableButton;
import com.navigaglobal.mobile.R;
import timber.log.Timber;

public class ActionButton extends ThemeableButton {

    private Operation operation;

    public ActionButton(Context context) {
        super(context);

    }

    public ActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ClickableAction);
            String actionName = typedArray.getString(R.styleable.ClickableAction_actionName);
            if (!TextUtils.isEmpty(actionName)) {
                String actionParameters = typedArray.getString(R.styleable.ClickableAction_actionParameters);
                JSONObject parameters = new JSONObject();
                if (!TextUtils.isEmpty(actionParameters)) {
                    String[] parts = actionParameters.split("\\|");
                    if (parts.length % 2 != 0) {
                        Timber.e("Invalid parameters for action" + actionName + " (not matching key/value): " + actionParameters);
                    }
                    else {
                        for (int i = 0; i < parts.length; i=i+2) {
                            String key = parts[i];
                            String value = parts[i+1];
                            try {
                                parameters.put(key, value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                operation = new Operation(actionName, null, parameters, null);
                if (GlobalActionHandler.getInstance().canPerform(getContext(), operation)) {
                    setOnClickListener(v -> GlobalActionHandler.getInstance().perform(getContext(), operation));
                }
            }
            typedArray.recycle();
        }
    }
}
