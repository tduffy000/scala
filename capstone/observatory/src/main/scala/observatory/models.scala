package observatory

/**
  * Class for the row records of temperature data points.
  * @param id       Identifier for station (STN-WBAN) where recording occurred; primary key.
  * @param month    Numerical month of recording.
  * @param day      Numerical day of recording.
  * @param temp     Decimal temperature recording (in Fahrenheit).
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
  *
  */
case class CombinedRecord(id: String, lat: Double, lon: Double,
                          year: Int, month: Int, day: Int, temperature: Double)

case class StationTempRecord(date: Date, location: Location, temperature: Double)

// need local date conversion
case class Date(year: Int, month: Int, day: Int)

/**
  * Introduced in Week 1. Represents a location on the globe.
  * @param lat Degrees of latitude, -90 ≤ lat ≤ 90
  * @param lon Degrees of longitude, -180 ≤ lon ≤ 180
  */
case class Location(lat: Double, lon: Double)

/**
  * Introduced in Week 3. Represents a tiled web map tile.
  * See https://en.wikipedia.org/wiki/Tiled_web_map
  * Based on http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
  * @param x X coordinate of the tile
  * @param y Y coordinate of the tile
  * @param zoom Zoom level, 0 ≤ zoom ≤ 19
  */
case class Tile(x: Int, y: Int, zoom: Int)

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
case class Color(red: Int, green: Int, blue: Int)
