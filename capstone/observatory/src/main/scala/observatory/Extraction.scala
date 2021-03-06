package observatory

import java.time.LocalDate

import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction {

  val spark: SparkSession =
      SparkSession
        .builder()
        .appName("Weather")
        .config("spark.master", "local")
        .getOrCreate()

  // suppress all [INFO] blocks printed on CL
  spark.sparkContext.setLogLevel("WARN")

  import spark.implicits._

  /**
    * @param p  Absolute path of the source datafile.
    * @return   Pathway to source datafile formatted properly.
    */
  def toPath( p: String ): String = getClass.getResource( p ).getPath

  /**
    * @param stationsFilePath   Pathway to .csv containing raw station data.
    * @return Dataset containing rows of StationRecord objects.
    */
  def getStationsFromPath( stationsFilePath: String ): Dataset[StationRecord] = {
    spark
      .read
      .csv( toPath(stationsFilePath) )
      .select(
          concat_ws("-", coalesce($"_c0", lit("")), $"_c1").alias("id"),
          $"_c2".cast(DoubleType).alias("lat"),
          $"_c3".cast(DoubleType).alias("lon")
      )
      .where($"_c2" =!= 0.0 && $"_c3" =!= 0.0
             && $"_c2".isNotNull && $"_c3".isNotNull)
      .as[StationRecord]
  }

  /**
    * @param  temperaturesFilePath  Pathway to .csv containing raw temperature data.
    * @param  year                  Numerical year of data.
    * @return Dataset containing rows of TemperatureRecord objects.
    */
  def getTemperaturesFromPath( temperaturesFilePath: String, year: Int ): Dataset[TemperatureRecord] = {
    spark
      .read
      .csv( toPath(temperaturesFilePath) )
      .select(
          concat_ws("-", coalesce($"_c0", lit("")), $"_c1").alias("id"),
          lit(year).alias("year"),
          $"_c2".cast(IntegerType).alias("month"),
          $"_c3".cast(IntegerType).alias("day"),
          (($"_c4" - 32) / 1.8 ).cast(DoubleType).alias("temperature")
      )
      .where($"_c4".between(-150, 150))
      .as[TemperatureRecord]
  }

  /**
    * @param  stations  Dataset containing rows of StationRecord objects.
    * @param  temps     Dataset containing rows of TemperatureRecord objects.
    * @return Dataset containing rows of CombinedRecord objects (i.e. the joined records)
    */
  def joinStationTemperatures( stations: Dataset[StationRecord], temps: Dataset[TemperatureRecord]): Dataset[StationTempRecord] = {
    stations
      .join(temps, "id")
      .as[CombinedRecord]
      .map( r => (Date(r.year, r.month, r.day), Location(r.lat, r.lon), r.temperature) )
      .toDF("date", "location", "temperature")
      .as[StationTempRecord]
  }

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    val joinedRecords = joinStationTemperatures( getStationsFromPath(stationsFile), getTemperaturesFromPath(temperaturesFile, year) )

    joinedRecords
     .collect()
     .par
     .map(
       jr => (jr.date.toLocalDate, jr.location, jr.temperature)
     )
     .seq
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    records
      .par
      .groupBy(_._2)
      .mapValues(
        loc => loc.foldLeft(0.0)(
          (acc, t) => acc + t._3
        ) / loc.size
      )
      .seq
  }

}
