package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import scala.math._

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val EarthRadius = 6372.8  // avg. radius of Earth in km

  /**
    * @param pointA Location a, first endpoint of line
    * @param pointB Location b, second endpoint of line
    * @return Distance between pointA & pointB (km)
    */
  def distance( pointA: Location, pointB: Location ): Double = {

    val Location(latA, lonA) = pointA
    val Location(latB, lonB) = pointB
    val latDist = toRadians(latA - latB)
    val lonDist = toRadians(lonA - lonB)

    val a = pow(sin(latDist / 2), 2) + cos(toRadians(latA)) * cos(toRadians(latB)) *
            pow(sin(lonDist / 2), 2)

    2 * atan2(sqrt(a), sqrt(1 - a)) * EarthRadius
  }

  /**
    * @param temps
    * @param loc
    * @return
    */
  def combineDistanceTemperature( temps: Iterable[(Location, Double)], loc: Location ): Iterable[(Double, Double)] = {
    temps.map{
      case (otherLoc, temp) => (loc.asPoint.haversineEarthDistace(otherLoc.asPoint), temp)
    }
  }

  /**
    * @param combinedDistTemp
    * @param p
    * @return
    */
  def inverseDistanceWeighting( combinedDistTemp: Iterable[(Double, Double)], p: Int): Double = {
    val (weightedSum, invWeightedSum) = combinedDistTemp.aggregate((0.0, 0.0))(
      {
        case((ws, iws), (dist, temp)) => {
          val w = pow(dist, -p)
          (w * temp + ws, w + iws) }
      }, {
        case ((wsA, iwsA), (wsB, iwsB)) => (wsA + wsB, iwsA + iwsB)
      }
    )
    weightedSum / invWeightedSum
  }

  /**
    * @param width
    * @param height
    * @param (pos)
    * @return
    */
  def posToLocation( width: Int, height: Int )(pos: Int): Location = {
    val wFactor = 180 * 2 / width.toDouble
    val hFactor = 90 * 2 / height.toDouble

    val x: Int = pos % width
    val y: Int = pos / width
    Location( 90 - (y * hFactor), (x * wFactor) - 180)
  }

  /**
    * @param a
    * @param b
    * @param x
    * @param colorA
    * @param colorB
    * @return
    */
  def interpolatePoint( a: Double, b: Double, x: Double )( colorA: Int, colorB: Int): Int = {
    val m = (x - a) / (b - a)
    round(colorA + m * (colorB - colorA)).toInt
  }

  /**
    * @param a
    * @param b
    * @param x
    * @return
    *
    */
  def interpolate( a: Option[(Double, Color)], b: Option[(Double, Color)], x: Double ): Color = (a, b) match {

    case (Some((aVal, colorA)), Some((bVal, colorB))) => {
      val ip = interpolatePoint(aVal, bVal, x)_
      Color(
        ip(colorA.red, colorB.red),
        ip(colorA.green, colorB.green),
        ip(colorA.blue, colorB.blue)
      )
    }
    case(Some(pointA), None) => pointA._2
    case(None, Some(pointB)) => pointB._2
    case _ => Color(0,0,0)
  }

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val preds: Iterable[(Double, Double)] = combineDistanceTemperature(temperatures, location)
    preds.find( p => p._1 == 0.0 ) match {
      case Some((_,temp)) => temp
      case _ => inverseDistanceWeighting(preds, 3)
    }
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value  The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    points.find( p => p._1 == value ) match {
      case Some((_, color)) => color
      case None => {
        val (a, b) = points.toList.sortBy(_._1).partition(_._1 < value)
        interpolate(a.reverse.headOption, b.headOption, value)
      }
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val Width = 360
    val Height = 180

    val locMap = posToLocation(Width, Height)_

    val pixels = (0.until(Height * Width)).par.map{
      pos => pos -> interpolateColor(
        colors,
        predictTemperature(
          temperatures,
          locMap(pos)
        )
      ).asPixel()
    }.seq.sortBy(_._1).map(_._2)

    Image(Width, Height, pixels.toArray)
  }

}
