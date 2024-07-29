package se.infomaker.livecontentui.section.adapter;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import io.reactivex.Single;
import se.infomaker.livecontentui.section.SectionItem;


public class Updater {


    private final List<SectionItem> oldItems;
    private final List<SectionItem> newItems;

    public Updater(List<SectionItem> oldItems, List<SectionItem> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    public Single<DiffUtil.DiffResult> calculateDiff() {
        return Single.create(emitter -> {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldItems != null ? oldItems.size() : 0;
                }

                @Override
                public int getNewListSize() {
                    return newItems != null ? newItems.size() : 0;
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    SectionItem old = oldItems.get(oldItemPosition);
                    SectionItem newItem = newItems.get(newItemPosition);
                    return old.isItemTheSame(newItem);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return oldItems.get(oldItemPosition).areContentsTheSame(newItems.get(newItemPosition));
                }
            });
            emitter.onSuccess(diffResult);
        });
    }
}
