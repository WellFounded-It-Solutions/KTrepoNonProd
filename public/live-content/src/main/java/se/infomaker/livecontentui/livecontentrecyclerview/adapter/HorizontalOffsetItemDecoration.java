package se.infomaker.livecontentui.livecontentrecyclerview.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.navigaglobal.mobile.livecontent.R;

public class HorizontalOffsetItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private final int mSidePadding;
    private int mOrientation;

    public HorizontalOffsetItemDecoration(Context context, int orientation) {
        mSidePadding = (int) context.getResources().getDimension(R.dimen.side_padding);
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(mSidePadding, 0, mSidePadding, 0);
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}