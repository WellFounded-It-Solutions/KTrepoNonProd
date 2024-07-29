package se.infomaker.livecontentui.livecontentrecyclerview.view;

public interface ContentInsetProvider {
    void addOnContentInsetChangedListener(OnContentInsetsChangedListener contentInsetsChangedListener);
    void removeOnContentInsetChangedListener(OnContentInsetsChangedListener contentInsetsChangedListener);
}
