package observatory

import java.time.LocalDate
import scala.math._
import com.sksamuel.scrimage.RGBColor

/**
  * Class for the row records of temperature data points.
  * @param id       Identifier for station (STN-WBAN) where recording occurred; primary key.
  * @param month    Numerical month of recording.
  * @param day      Numerical day of recording.
  * @param temp     Decimal temperature recording (in Celsius).
  */
case class TemperatureRecord(id: String, month: Int, day: Int, temperature: Double)

/**
  * Class for the row records of the station locations data points.
  * @param id       Identifier for station (STN-WBAN) where recording occurred; primary key.
  * @param lat      latitude of station in degrees, -90 ≤ lat ≤ 90
  * @param lon      longtitude of station in degrees, -180 ≤ lon ≤ 180
  */
case class StationRecord(id: String, lat: Double, lon: Double)

/**
  * Class for the combined record of fields from the stations & temperature records.
  * @param id           Identifier for station (STN-WBAN) where recording occurred; primary key.
  * @param lat          latitude of station in degrees, -90 ≤ lat ≤ 90
  * @param lon          longtitude of station in degrees, -180 ≤ lon ≤ 180
  * @param year         Numerical year of recording.
  * @param month        Numerical month of recording.
  * @param day          Numerical day of recording.
  * @param temperature  Decimal temperature recording (in Celsius)
  */
case class CombinedRecord(id: String, lat: Double, lon: Double,
                          year: Int, month: Int, day: Int, temperature: Double)

/**
  * Class for the final records for station-specific temperature readings.
  * @param date         Date of recording using Date class.
  * @param location     Location of station for record using Location class.
  * @param temperature  Decimal temperature recording (in Celsius)
  *
  */
case class StationTempRecord(date: Date, location: Location, temperature: Double)

/**
  * Class for the Date of the observation.
  * @param year   Numerical year of recording.
  * @param month  Numerical month of recording.
  * @param day    Numerical day of recording.
  */
case class Date(year: Int, month: Int, day: Int) {
  def toLocalDate = LocalDate.of(year, month, day)
}

/**
  * Class to represent Locations as coordinates on a chord to obtain accurate
  * distance between them.
  * See https://en.wikipedia.org/wiki/Great-circle_distance
  * See https://en.wikipedia.org/wiki/Haversine_formula
  * @param theta  Measure of angle along z plane
  * @param lambda Measure of angle along x plane
  */
case class Point(theta: Double, lambda: Double) {
  lazy val location: Location = Location(toDegrees(theta), toDegrees(lambda))
  val EarthRadius = 6372.8

  /**
    * @param other  Point b (endpoint) for distance
    * @return Distance between a (this) & b (other) in radians
    */
  def greatCircleDistance(other: Point): Double = {
    val deltaTheta = abs(other.theta - theta)
    val deltaLambda = abs(other.lambda - lambda)

    val a = pow(sin(deltaTheta / 2), 2) + cos(theta) *
            cos(other.theta) * pow(sin(deltaLambda / 2), 2)
    2 * atan2(sqrt(a), sqrt(1-a))
  }

  /**
    * @param other Point b (endpoint) for distance
    * @return Distance between a (this) & b (other) in meters
    */
  def haversineEarthDistace(other: Point): Double =
    EarthRadius * greatCircleDistance(other) * 1000

}

/**
  * Introduced in Week 1. Represents a location on the globe.
  * @param lat Degrees of latitude, -90 ≤ lat ≤ 90    val point:
  * @param lon Degrees of longitude, -180 ≤ lon ≤ 180
  */
case class Location(lat: Double, lon: Double) {
  lazy val asPoint: Point = Point(toRadians(lat), toRadians(lon))
}

/**
  * Introduced in Week 3. Represents a tiled web map tile.
  * See https://en.wikipedia.org/wiki/Tiled_web_map
  * Based on http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
  * @param x X coordinate of the tile
  * @param y Y coordinate of the tile
  * @param zoom Zoom level, 0 ≤ zoom ≤ 19
  */
case class Tile(x: Int, y: Int, zoom: Int) {
  lazy val location: Location = Location(
    lat = toDegrees(atan(sinh(Pi * (1.0 - 2.0 * y / (1 << zoom))))),
    lon = x / ( 1 << zoom ) * 360.0 - 180.0
  )

  def toURI = new java.net.URI("http://tile.openstreetmap.org/" +
                               zoom + "/" + x + "/" + y + ".png")
}

/**
  * Introduced in Week 4. Represents a point on a grid composed of
  * circles of latitudes and lines of longitude.
  * @param lat Circle of latitude in degrees, -89 ≤ lat ≤ 90
  * @param lon Line of longitude in degrees, -180 ≤ lon ≤ 179
  */
case class GridLocation(lat: Int, lon: Int)

/**
  * Introduced in Week 5. Represents a point inside of a grid cell.
  * @param x X coordinate inside the cell, 0 ≤ x ≤ 1
  * @param y Y coordinate inside the cell, 0 ≤ y ≤ 1
  */
case class CellPoint(x: Double, y: Double)

/**
  * Introduced in Week 2. Represents an RGB color.
  * @param red Level of red, 0 ≤ red ≤ 255
  * @param green Level of green, 0 ≤ green ≤ 255
  * @param blue Level of blue, 0 ≤ blue ≤ 255
  */
case class Color(red: Int, green: Int, blue: Int) {

  /**
    * @param alpha Shading of particular pixel in range [0, 256)
    * @return RGB representation of pixel (for image)
    */
  def asPixel(alpha: Int = 255) = RGBColor(red, green, blue, alpha).toPixel
}
