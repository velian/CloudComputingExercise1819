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
import org.apache.flink.api.common.functions._
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFields
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem

import scala.collection.JavaConverters._

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

    val t = cellTowers
      .map(new IsInRange).withBroadcastSet(spots, "spots")

    val results = spots

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

  /**
   * A simple two-dimensional point.
   */
  case class CellTower(var radio: String = "", var mnc: Int = 0, var longitude: Double = 0, var latitude: Double  = 0, var range: Double = 0) extends Coordinate

  /**
   * A simple two-dimensional point.
   */
  case class Point(var latitude: Double = 0, var longitude: Double = 0) extends Coordinate


  /** Determines the closest cluster center for a data point. */
  @ForwardedFields(Array("*->_2"))
  final class IsInRange extends RichMapFunction[CellTower, Boolean] {
    private var spots: Traversable[Point] = null

    /** Reads the centroid values from a broadcast variable into a collection. */
    override def open(parameters: Configuration) {
      spots = getRuntimeContext.getBroadcastVariable[Point]("spots").asScala
    }

    def map(p: CellTower): Boolean = {
      for (centroid <- spots) {
        val distance = p.haversineDistance(centroid)
        val reaches = distance <= p.range
        if (reaches) {
          return true
        }
      }
      false
    }

  }
}