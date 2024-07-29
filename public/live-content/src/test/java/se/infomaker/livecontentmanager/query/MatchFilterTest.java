package se.infomaker.livecontentmanager.query;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class MatchFilterTest {
    @Test
    public void testSearchQuery() {
        MatchFilter matchFilter = new MatchFilter("elefant", "zebra");
        String searchQuery = matchFilter.createSearchQuery("base");
        assertThat(searchQuery).isEqualTo("(base) AND elefant:\"zebra\"");
    }
}
