package se.infomaker.livecontentui.section.adapter;

import java.util.List;

import se.infomaker.livecontentui.section.Section;
import se.infomaker.livecontentui.section.SectionItem;

/*
  This implementation is simple and in efficient
  TODO add caching and cache invalidating to improve performance!
 */

/**
 * Provides index/type mapping for sections
 */
public class SectionIndexRouter {

    private final List<Section> sectionList;

    public SectionIndexRouter(List<Section> sectionList) {
        this.sectionList = sectionList;
    }

    /**
     *
     * @param position
     * @return the item for the position
     */
    public SectionItem itemForIndex(int position) {
        return sectionForIndex(position).item(sectionInternalIndex(position));
    }

    /**
     *
     * @param position to get section for
     * @return section containing index
     * @throws IndexOutOfBoundsException position is out of bounds
     */
    public Section sectionForIndex(int position) throws IndexOutOfBoundsException {
        int index = 0;
        for (Section section : sectionList) {
            index += section.size();
            if (section.size() > 0 && index > position) {
                return section;
            }
        }
        throw new IndexOutOfBoundsException("No item at position: " + position);
    }

    /**
     * Provides the internal index for the position in the responsible section
     *
     * @param position
     * @return internal index
     * @throws IndexOutOfBoundsException position is out of bounds
     */
    public int sectionInternalIndex(int position) {
        int left = position;
        for (Section section : sectionList) {
            if (left < section.size()) {
                return left;
            }
            left -= section.size();
        }
        throw new IndexOutOfBoundsException("No item at position: " + position);
    }

    public int totalItemCount() {
        int size = 0;
        for (Section section : sectionList) {
            size += section.size();
        }
        return size;
    }
}
