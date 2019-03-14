package observatory

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Double)]): GridLocation => Temperature = {
    val grid: Map[GridLocation, Temperature] = {
      for {
        lat <- -89 to 90
        lon <- -180 to 179
      } yield GridLocation(lat, lon) -> Visualization.predictTemperature(temperatures, Location(lat, lon))
    }.toMap

    g => grid(GridLocation(g.lat, g.lon))
  }

  /**
    * @param temperatureSeqs Sequence of known temperatures over the years (each element of the collection
    *                        is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatureSeqs: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val grids: Iterable[GridLocation => Temperature] = temperatureSeqs.map(makeGrid)
    g => {
      val t = grids.map(grid => grid(g))
      t.sum / t.size
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Double)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val grid = makeGrid(temperatures)
    g => grid(g) - normals(g)
  }

}
