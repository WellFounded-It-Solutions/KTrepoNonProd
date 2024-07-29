package se.infomaker.livecontentui.livecontentrecyclerview.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by davidolsson on 10/13/15.
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 15; // The minimum amount of items to have below your current scroll position before loading more.

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int firstVisibleItem, visibleItemCount, totalItemCount, offset;

    private final LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        update();

        if (firstVisibleItem <= 0){
            onTopPosition();
        }

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        checkIfLoadMore();
    }

    private void update() {
        visibleItemCount = mLinearLayoutManager.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
    }

    private void checkIfLoadMore() {
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
            onLoadMore(totalItemCount + offset);
            loading = true;
        }
    }

    public abstract void onLoadMore(int totalItemCount);

    public abstract void onTopPosition();

    public void reset() {
        this.previousTotal = 0;
        this.offset = 0;
        this.loading = false;
        update();
        checkIfLoadMore();
    }

    public void increaseOffset() {
        this.offset++;
    }
}
