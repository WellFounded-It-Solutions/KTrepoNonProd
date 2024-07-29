package se.infomaker.livecontentmanager.query

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import se.infomaker.livecontentmanager.JUnitTree
import timber.log.Timber

class LocationFilterTest {
    companion object {
        private val RADIUS_METERS = 478889.40994093416
        private val RADIUS_DEGREES = 4.3055409199196806
        private val LATITUDE = 57.781046
        private val LONGITUDE = 14.161206
        private val BASE_QUERY = "BASE-QUERY"
        private val GEO_POINTS_KEY = "GeoPoints"
    }

    lateinit var locationFilter: LocationFilter

    @Before
    fun setup() {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
        locationFilter = LocationFilter(RADIUS_METERS, LATITUDE, LONGITUDE, GEO_POINTS_KEY)
    }

    @Test
    fun testIdentifier() {
        val expected = "radius:$RADIUS_METERS:lat:$LATITUDE:lng:$LONGITUDE"
        Assert.assertEquals(expected, locationFilter.identifier())
    }

    @Test
    fun testCreateStreamFilter() {
        val streamFilterObject = locationFilter.createStreamFilter()
        val expected = "{\"geo_shape\":{\"$GEO_POINTS_KEY\":{\"shape\":{\"type\":\"circle\",\"radius\":\"${RADIUS_METERS}m\",\"coordinates\":[$LATITUDE,$LONGITUDE]}}}}"
        JSONAssert.assertEquals(expected, streamFilterObject, false)
    }

    @Test
    fun testCreateSearchQuery() {
        val expectedQuery = "($BASE_QUERY) AND $GEO_POINTS_KEY:\"Intersects(BUFFER(POINT($LONGITUDE $LATITUDE), $RADIUS_DEGREES))\""
        Assert.assertEquals(expectedQuery, locationFilter.createSearchQuery(BASE_QUERY))
    }

    @Test
    fun testConvertToDegrees() {
        Assert.assertEquals(RADIUS_DEGREES, locationFilter.convertToDegrees(RADIUS_METERS), 0.001)
    }
}