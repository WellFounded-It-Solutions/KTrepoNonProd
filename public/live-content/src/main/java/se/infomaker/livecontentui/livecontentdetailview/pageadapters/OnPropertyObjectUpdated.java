package se.infomaker.livecontentui.livecontentdetailview.pageadapters;

import se.infomaker.livecontentmanager.parser.PropertyObject;

public interface OnPropertyObjectUpdated {
    /**
     * Called when the object is updated
     * objects are identified using object.getId()
     * @param object
     */
    void onObjectUpdated(PropertyObject object);
}
