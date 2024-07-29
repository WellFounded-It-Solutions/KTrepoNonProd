package se.infomaker.livecontentmanager.query

import org.json.JSONArray
import org.json.JSONObject

/**
 * A filter for position
 * @param radius in meters of the location
 * @param latitude
 * @param longitude
 */
open class LocationFilter(val radius: Double, val latitude: Double, val longitude: Double, val geoPointsKey: String) : QueryFilter {
    override fun identifier(): String {
        return "radius:$radius:lat:$latitude:lng:$longitude"
    }

    override fun createStreamFilter(): JSONObject {
        val filterObject = JSONObject()
        val geo_shapeObject = JSONObject()
        val geoPointsObject = JSONObject()
        val shapeObject = JSONObject()

        val shapeCoordinatesArray = JSONArray()
        shapeCoordinatesArray.put(longitude)
        shapeCoordinatesArray.put(latitude)

        shapeObject.put("coordinates", shapeCoordinatesArray)
        shapeObject.put("radius", "${radius}m")
        shapeObject.put("type", "circle")

        geoPointsObject.put("shape", shapeObject)
        geo_shapeObject.put(geoPointsKey, geoPointsObject)
        filterObject.put("geo_shape", geo_shapeObject)

        return filterObject
    }

    override fun createSearchQuery(baseQuery: String): String {
        return "($baseQuery) AND $geoPointsKey:\"Intersects(BUFFER(POINT($longitude $latitude), ${convertToDegrees(radius)}))\""
    }

    /**
     * @param radius in meters
     * @return degrees for use in search query
     */
    fun convertToDegrees(radius: Double): Double {
        // Sanity checking as this conversion is only an approximation
        return Math.min(90.0, (radius / 1000 / (40041.47 / 360.0)))
    }
}