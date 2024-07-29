package se.infomaker.iap.map

data class MapOptions(val coordinates: Coordinates, val zoomLevel: Float, val interaction: Interaction)

data class Coordinates(val latitude: Double, val longitude: Double)

enum class Interaction {
    INTERACTIVE, STATIC, EXTERNAL
}