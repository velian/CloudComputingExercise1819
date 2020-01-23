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
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.scala._


object SocketWindowWordCount {

  def main(args: Array[String]) : Unit = {

    val params = ParameterTool.fromArgs(args)
    val input = if (params.has("input")) params.get("input") else "tolstoy-war-and-peace.txt"
    val output = if (params.has("output")) params.get("output") else "output.txt"

    // get the execution environment
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val text: DataStream[String] = env.readTextFile(input)

    // parse the data, group it, window it, and aggregate the counts
    val windowCounts = text
      .flatMap { w => w.split("\\s") }
      .map { w => w.replaceAll("[^A-Za-z]", "");}
      .map { w => w.toLowerCase()}
      .filter( w => w != "")
      .map { w => WordWithCount(w, 1) }
      .keyBy("word")
      .sum("count")

    windowCounts.writeAsCsv(output, FileSystem.WriteMode.OVERWRITE, "\n", ",")

    env.execute("Socket Window WordCount")
  }


  /** Data type for words with count */
  case class WordWithCount(word: String, count: Long)
}