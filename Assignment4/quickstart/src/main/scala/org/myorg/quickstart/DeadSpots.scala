/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.myorg.quickstart
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem

object DeadSpots {

  def main(args: Array[String]) {

    // checking input parameters
    val params: ParameterTool = ParameterTool.fromArgs(args)
    val mnc_string = if (params.has("mnc")) params.get("mnc") else ""

    val mncs = if (mnc_string == "") Array() else mnc_string
      .split(",")
      .map(mnc => mnc.toInt)

    // set up execution environment
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

    // get input data:
    val cellTowers: DataSet[CellTower] = getInputDataSet(params, env)
    val spots: DataSet[Point] = getSpotsDataSet(params, env)

    val filteredCellTowers: DataSet[CellTower] = cellTowers
      .filter( cell => mncs.isEmpty || mncs.contains(cell.mnc))

    val cellTowersGSM = filteredCellTowers.filter(cell => cell.radio == "GSM")
    val cellTowersUMTS = filteredCellTowers.filter(cell => cell.radio == "UMTS")
    val cellTowersLTE = filteredCellTowers.filter(cell => cell.radio == "LTE")

    val coverageGSM = cellTowersGSM
      .crossWithTiny(spots)
      .map(element => (element._2.longitude, element._2.latitude, element._2.haversineDistance(element._1) - element._1.range))
      .map(spot => (spot._1, spot._2, spot._3 <= 0))
      .groupBy(spot => (spot._1, spot._2))
      .reduce {(spot1, spot2) => (spot1._1, spot1._2, spot1._3 || spot2._3)}
      .map(spot => (spot._1, spot._2, (spot._3, false, false)))

    val coverageUMTS = cellTowersUMTS
      .crossWithTiny(spots)
      .map(element => (element._2.longitude, element._2.latitude, element._2.haversineDistance(element._1) - element._1.range))
      .map(spot => (spot._1, spot._2, spot._3 <= 0))
      .groupBy(spot => (spot._1, spot._2))
      .reduce {(spot1, spot2) => (spot1._1, spot1._2, spot1._3 || spot2._3)}
      .map(spot => (spot._1, spot._2, (false, spot._3, false)))

    val coverageLTE = cellTowersLTE
      .crossWithTiny(spots)
      .map(element => (element._2.longitude, element._2.latitude, element._2.haversineDistance(element._1) - element._1.range))
      .map(spot => (spot._1, spot._2, spot._3 <= 0))
      .groupBy(spot => (spot._1, spot._2))
      .reduce {(spot1, spot2) => (spot1._1, spot1._2, spot1._3 || spot2._3)}
      .map(spot => (spot._1, spot._2, (false, false, spot._3)))

    val results = coverageGSM
      .union(coverageUMTS)
      .union(coverageLTE)
      .groupBy(spot => (spot._1, spot._2))
      .reduce {(spot1, spot2) => (spot1._1, spot1._2, (spot1._3._1 || spot2._3._1, spot1._3._2 || spot2._3._2, spot1._3._3 || spot2._3._3))}
      .map(spot => Result(spot._1, spot._2, spot._3._1, spot._3._2, spot._3._3))

    if (params.has("output")) {
      results.writeAsCsv(params.get("output"), "\n", ",", FileSystem.WriteMode.OVERWRITE)
      env.execute("Scala DeadSpots")
    } else {
      println("Printing result to stdout. Use --output to specify output path.")
      results.print()
    }

  }

  // *************************************************************************
  //     UTIL FUNCTIONS
  // *************************************************************************

  def getInputDataSet(params: ParameterTool, env: ExecutionEnvironment): DataSet[CellTower] = {
    env.readCsvFile[CellTower](
      params.get("input"),
      fieldDelimiter = ",",
      includedFields = Array(0, 2, 6, 7, 8),
      ignoreFirstLine = true)
  }

  def getSpotsDataSet(params: ParameterTool, env: ExecutionEnvironment): DataSet[Point] = {
    env.readCsvFile[Point](
      params.get("spots"),
      fieldDelimiter = ",",
      includedFields = Array(0, 1),
      ignoreFirstLine = true)
  }

  // *************************************************************************
  //     DATA TYPES
  // *************************************************************************

  /**
   * Common trait for operations supported by both points and centroids
   * Note: case class inheritance is not allowed in Scala
   */
  trait Coordinate extends Serializable {

    var latitude: Double
    var longitude: Double

    def add(other: Coordinate): this.type = {
      latitude += other.latitude
      longitude += other.longitude
      this
    }

    def div(other: Long): this.type = {
      latitude /= other
      longitude /= other
      this
    }

    def haversineDistance(other: Coordinate): Double = {
      val R = 6371000
      val lat1 = this.latitude.toRadians
      val lat2 = other.latitude.toRadians
      val latDiff = (other.latitude - this.latitude).toRadians
      val longDiff = (other.longitude - this.longitude).toRadians

      val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(longDiff / 2) * Math.sin(longDiff / 2)

      val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
      val distance = R * c

      distance
    }

    def clear(): Unit = {
      latitude = 0
      longitude = 0
    }

    override def toString: String =
      s"$latitude,$longitude"

  }

  case class CellTower(var radio: String = "", var mnc: Int = 0, var longitude: Double = 0, var latitude: Double  = 0, var range: Double = 0) extends Coordinate

  case class Result(var longitude: Double = 0, var latitude: Double  = 0, var gsm: Boolean, var umts: Boolean, var lte: Boolean) extends Coordinate

  /**
   * A simple two-dimensional point.
   */
  case class Point(var longitude: Double = 0, var latitude: Double = 0) extends Coordinate
}