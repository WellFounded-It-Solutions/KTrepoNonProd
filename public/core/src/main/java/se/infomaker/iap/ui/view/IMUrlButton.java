package se.infomaker.iap.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;

import com.navigaglobal.mobile.R;


public class IMUrlButton extends AppCompatButton {
    private String url;

    public IMUrlButton(Context context) {
        this(context, null);
    }

    public IMUrlButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.buttonStyle);
    }

    public IMUrlButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.IMUrlButton, defStyleAttr, defStyleAttr);
        url = typedArray.getString(R.styleable.IMUrlButton_url);
        typedArray.recycle();
        if (url != null) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    getContext().startActivity(intent);
                }
            });
        }
    }
}
