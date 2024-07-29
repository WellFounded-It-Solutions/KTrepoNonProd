package se.infomaker.livecontentmanager.query;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Date;

public class CatchupFilterTest {
    @Test
    public void testSearchQuery() {
        Date date = new Date(484012800000l);
        String searchQuery = new CatchupFilter(date, "Pubdate").createSearchQuery("base");
        Assert.assertEquals("Pubdate:[1985-05-04T00:00:00Z TO NOW] AND (base)", searchQuery);
    }
}
