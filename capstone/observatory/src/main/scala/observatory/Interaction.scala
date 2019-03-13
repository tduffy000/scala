package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization._

/**
  * 3rd milestone: interactive visualization
  */
/*
 Label of failing property: Incorrect computed color at Location(-27.059125784374057,-180.0): Color(247,0,8).
 Expected to be closer to Color(0,0,255) than Color(255,0,0) [Lost Points] 5
*/
object Interaction {

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = tile.location

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val Width = 256
    val Height = 256

    val pixels = ( (0).until(Width * Height) )
                 .par
                 .map(
                   pos => {
                     val xPos = (( pos % Width ).toDouble / Width + tile.x).toInt
                     val yPos = (( pos / Height ).toDouble / Height + tile.y).toInt

                     pos -> interpolateColor(
                       colors,
                       predictTemperature(
                         temperatures,
                         Tile(xPos, yPos, tile.zoom).location
                       )
                     ).asPixel(127)
                   }
                 ).seq
                  .sortBy(_._1)
                  .map(_._2)

    Image(Width, Height, pixels.toArray)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {
    val _ = for {
      (yr, data) <- yearlyData
      zoom <- 0 to 3
      x    <- 0 until 1 << zoom
      y    <- 0 until 1 << zoom
    } {
      generateImage(yr, Tile(x, y, zoom), data)
    }
  }

}
