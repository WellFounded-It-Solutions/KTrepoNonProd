package se.infomaker.livecontentmanager;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.infomaker.livecontentmanager.query.CatchupFilter;
import se.infomaker.livecontentmanager.query.FilterHelper;
import se.infomaker.livecontentmanager.query.LocationFilter;
import se.infomaker.livecontentmanager.query.QueryFilter;

@RunWith(AndroidJUnit4.class)
public class FilterHelperTest {

    @Test
    public void putAndGet()
    {
        Intent intent = new Intent("");
        ArrayList<QueryFilter> filters = new ArrayList<>();
        filters.add(new LocationFilter(1000, 10, 50, "brum"));
        filters.add(new LocationFilter(100, 11, 55, "nuff"));
        filters.add(new CatchupFilter(new Date(), "pubDate"));
        FilterHelper.put(intent, filters);

        List<QueryFilter> filters1 = FilterHelper.getFilters(intent);
        Assert.assertNotNull(filters1);
        for (int i = 0; i <filters.size(); i++) {
            Assert.assertTrue(filters.get(i).equals(filters1.get(i)));
        }
        Assert.assertEquals(3, filters1.size());
    }

    @Test
    public void getEmpty()
    {
        List<QueryFilter> filters = FilterHelper.getFilters(new Intent(""));
        Assert.assertNull(filters);
    }

    @Test
    public void putEmpty()
    {
        Intent intent = new Intent("");
        FilterHelper.put(intent, null);
        List<QueryFilter> filters = FilterHelper.getFilters(intent);
        Assert.assertNull(filters);
    }
}
