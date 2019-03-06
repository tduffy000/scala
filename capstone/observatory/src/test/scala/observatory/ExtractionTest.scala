package observatory

import org.apache.spark.sql.Dataset
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.io.Source

trait ExtractionTest extends FunSuite {

  val year = 1990
  val verbose = true

  val stationPath = "/stations.csv"
  val temperaturePath = s"/$year.csv"

  lazy val stations: Dataset[StationRecord] = Extraction.getStationsFromPath(stationPath).cache
  lazy val temperatures: Dataset[TemperatureRecord] = Extraction.getTemperaturesFromPath(temperaturePath, year).cache
  lazy val stationTemps: Dataset[StationTempRecord] = Extraction.joinStationTemperatures(stations, temperatures).cache

  lazy val locationTemperatures = Extraction.locateTemperatures(year, stationPath, temperaturePath)
  lazy val locationAverage = Extraction.locationYearlyAverageRecords(locationTemperatures)

  test("stations import") {
    if( verbose ) stations.show()
    // assertions
  }

  test("temperatures import") {
    if( verbose ) temperatures.show()
    // assertions
  }

  test("join stations & temperatures") {
    if( verbose ) stationTemps.show()
    // assertions
  }

  test("group by location") {
    // assertions
  }

  test("average temperatures") {
    // assertions
  }

}
