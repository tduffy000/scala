package observatory

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.{min,max}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.apache.spark.sql.Row

trait ExtractionTest extends FunSuite {

  val year = 1990
  val verbose = true

  val stationPath = "/stations.csv"
  val temperaturePath = s"/$year.csv"

  lazy val stations: Dataset[StationRecord] = Extraction.getStationsFromPath(stationPath)//.persist
  lazy val temperatures: Dataset[TemperatureRecord] = Extraction.getTemperaturesFromPath(temperaturePath, year)//.persist
  lazy val stationTemps: Dataset[StationTempRecord] = Extraction.joinStationTemperatures(stations, temperatures)//.persist

  lazy val locationTemperatures = Extraction.locateTemperatures(year, stationPath, temperaturePath)
  lazy val locationAverage = Extraction.locationYearlyAverageRecords(locationTemperatures)

  test("stations import") {
    if( verbose ) stations.show()
    assert( stations.count() == 27708, "total rows = 27,708" )
    assert( stations.filter( (s: StationRecord) => s.id == "010010" ).count === 1, "010010 exists" )
    assert( stations.filter( (s: StationRecord) => s.id == "011100" ).select("lat", "lon").head === Row(66.0, 11.683), "011100 lat/lon test" )
    assert( stations.filter( (s: StationRecord) => s.id == "011134" ).count === 0, " 011134 does not exist" )
    assert( stations.agg(min("lat"), max("lat")).head === Row(-89.0, 89.37), "min/max latitude" )
    assert( stations.agg(min("lon"), max("lon")).head === Row(-179.983, 179.75), "min/max longitude" )
    }

  test("temperatures import") {
    if( verbose ) temperatures.show()
    assert( temperatures.count() == 2616141, "total rows = 2,616,141")
    // assert( temperatures.agg(avg("temperature")).head === Row(11.827956138449727), "avg. temperature (C)")
    // temperature range
  }
  /*
  test("join stations & temperatures") {
    if( verbose ) stationTemps.show()


  }

  test("group by location") {
    if( verbose ) locationTemperatures.take(10).foreach(println)

  }

  test("average temperatures") {
    // assertions
    if( verbose ) locationAverage.take(10).foreach(println)
  }
  */
}
