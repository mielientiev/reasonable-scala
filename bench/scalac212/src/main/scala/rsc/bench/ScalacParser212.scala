// Copyright (c) 2017 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.bench

import java.nio.file._
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.annotations.Mode._
import scala.reflect.io._
import scala.reflect.internal.util._
import scala.tools.nsc._
import scala.tools.nsc.reporters._
import rsc.bench.ScalacParser212._

object ScalacParser212 {
  @State(Scope.Benchmark)
  class BenchmarkState extends FileFixtures {
    val settings = new Settings
    settings.outdir.value = Files.createTempDirectory("scalac_").toString
    settings.usejavacp.value = true
    val reporter = new StoreReporter
    val global = Global(settings, reporter)
    val run = new global.Run
    val abstractFiles = re2sScalacFiles.map(f => AbstractFile.getFile(f))
    val sourceFiles = abstractFiles.map(f => new BatchSourceFile(f)).toArray
  }
}

trait ScalacParser212 {
  def runImpl(bs: BenchmarkState): Unit = {
    var i = 0
    while (i < bs.sourceFiles.length) {
      val sourceFile = bs.sourceFiles(i)
      val parser = new bs.global.syntaxAnalyzer.SourceFileParser(sourceFile)
      parser.parse()
      i += 1
    }
    if (bs.reporter.hasErrors) {
      bs.reporter.infos.foreach(println)
      sys.error("parse failed")
    }
  }
}

@BenchmarkMode(Array(SingleShotTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 128, jvmArgs = Array("-Xms2G", "-Xmx2G"))
class ColdScalacParser212 extends ScalacParser212 {
  @Benchmark
  def run(bs: BenchmarkState): Unit = {
    runImpl(bs)
  }
}

@BenchmarkMode(Array(SampleTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = Array("-Xms2G", "-Xmx2G"))
class WarmScalacParser212 extends ScalacParser212 {
  @Benchmark
  def run(bs: BenchmarkState): Unit = {
    runImpl(bs)
  }
}

@BenchmarkMode(Array(SampleTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = Array("-Xms2G", "-Xmx2G"))
class HotScalacParser212 extends ScalacParser212 {
  @Benchmark
  def run(bs: BenchmarkState): Unit = {
    runImpl(bs)
  }
}
