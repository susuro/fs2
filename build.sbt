import microsites.ExtraMdFileConfig
import com.typesafe.tools.mima.core._
import sbtcrossproject.crossProject

addCommandAlias("fmt", "; compile:scalafmt; test:scalafmt; it:scalafmt; scalafmtSbt")
addCommandAlias(
  "fmtCheck",
  "; compile:scalafmtCheck; test:scalafmtCheck; it:scalafmtCheck; scalafmtSbtCheck"
)
addCommandAlias("testJVM", ";coreJVM/test;io/test;reactiveStreams/test;benchmark/test")
addCommandAlias("testJS", "coreJS/test")

ThisBuild / baseVersion := "2.4"

ThisBuild / organization := "co.fs2"
ThisBuild / organizationName := "Functional Streams for Scala"

ThisBuild / homepage := Some(url("https://github.com/typelevel/fs2"))
ThisBuild / startYear := Some(2013)

ThisBuild / crossScalaVersions := Seq("2.13.3", "2.12.10", "0.27.0-RC1")

ThisBuild / versionIntroduced := Map(
  "0.26.0-RC1" -> "2.4.99"
)

ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.11")

ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.Equals(Ref.Branch("develop"))
)

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("fmtCheck", "compile")),
  WorkflowStep.Sbt(List("testJVM")),
  WorkflowStep.Sbt(List("testJS")),
  WorkflowStep.Sbt(List("mimaReportBinaryIssues")),
  WorkflowStep.Sbt(List("project coreJVM", "it:test"))
)

ThisBuild / githubWorkflowEnv ++= Map(
  "SONATYPE_USERNAME" -> "fs2-ci",
  "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
  "PGP_SECRET" -> s"$${{ secrets.PGP_SECRET }}"
)

ThisBuild / githubWorkflowTargetTags += "v*"

ThisBuild / githubWorkflowPublishPreamble +=
  WorkflowStep.Run(
    List("echo $PGP_SECRET | base64 -d | gpg --import"),
    name = Some("Import signing key")
  )

ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("release")))

ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/typelevel/fs2"), "git@github.com:typelevel/fs2.git")
)

ThisBuild / licenses := List(("MIT", url("http://opensource.org/licenses/MIT")))

ThisBuild / testFrameworks += new TestFramework("munit.Framework")
ThisBuild / doctestTestFramework := DoctestTestFramework.ScalaCheck

ThisBuild / publishGithubUser := "mpilquist"
ThisBuild / publishFullName := "Michael Pilquist"
ThisBuild / developers ++= List(
  "pchiusano" -> "Paul Chiusano",
  "pchlupacek" -> "Pavel Chlupáček",
  "SystemFw" -> "Fabio Labella",
  "alissapajer" -> "Alissa Pajer",
  "djspiewak" -> "Daniel Spiewak",
  "fthomas" -> "Frank Thomas",
  "runarorama" -> "Rúnar Ó. Bjarnason",
  "jedws" -> "Jed Wesley-Smith",
  "durban" -> "Daniel Urban"
).map {
  case (username, fullName) =>
    Developer(username, fullName, s"@$username", url(s"https://github.com/$username"))
}

ThisBuild / fatalWarningsInCI := false

ThisBuild / Test / javaOptions ++= Seq(
  "-Dscala.concurrent.context.minThreads=8",
  "-Dscala.concurrent.context.numThreads=8",
  "-Dscala.concurrent.context.maxThreads=8"
)
ThisBuild / Test / run / javaOptions ++= Seq("-Xms64m", "-Xmx64m")
ThisBuild / Test / parallelExecution := false

ThisBuild / initialCommands := s"""
    import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
    import scala.concurrent.ExecutionContext.Implicits.global, scala.concurrent.duration._
    implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
    implicit val timerIO: Timer[IO] = IO.timer(global)
  """

ThisBuild / mimaBinaryIssueFilters ++= Seq(
  // These methods were only used internally between Stream and Pull: they were private to fs2.
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.fromFreeC"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.get$extension"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream#IdOps.self$extension"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Pull.get$extension"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Pull.get"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.get$extension"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.get"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Pull.fromFreeC"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Pull.get$extension"),
  // No bincompat on internal package
  ProblemFilters.exclude[Problem]("fs2.internal.*"),
  // Mima reports all ScalaSignature changes as errors, despite the fact that they don't cause bincompat issues when version swapping (see https://github.com/lightbend/mima/issues/361)
  ProblemFilters.exclude[IncompatibleSignatureProblem]("*"),
  // .to(sink) syntax was removed in 1.0.2 and has been hidden in all 2.x releases behind private[fs2], hence it's safe to remove
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.to"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.to$extension"),
  ProblemFilters.exclude[DirectMissingMethodProblem](
    "fs2.interop.reactivestreams.StreamSubscriber#FSM.stream"
  ), // FSM is package private
  ProblemFilters.exclude[Problem]("fs2.io.tls.TLSEngine.*"), // private[fs2] type
  ProblemFilters.exclude[Problem]("fs2.io.tls.TLSEngine#*"),
  ProblemFilters.exclude[DirectMissingMethodProblem](
    "fs2.io.tls.TLSSocket.fs2$io$tls$TLSSocket$$binding$default$2"
  ),
  ProblemFilters.exclude[DirectMissingMethodProblem](
    "fs2.io.tls.TLSSocket.fs2$io$tls$TLSSocket$$binding$default$3"
  ),
  // InputOutputBuffer is private[tls]
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.io.tls.InputOutputBuffer.output"),
  ProblemFilters.exclude[ReversedMissingMethodProblem]("fs2.io.tls.InputOutputBuffer.output"),
  // Private traits for implicit prioritization
  ProblemFilters.exclude[ReversedMissingMethodProblem](
    "fs2.Stream#LowPrioCompiler.fs2$Stream$LowPrioCompiler$_setter_$fallibleInstance_="
  ),
  ProblemFilters.exclude[ReversedMissingMethodProblem](
    "fs2.Stream#LowPrioCompiler.fallibleInstance"
  ),
  ProblemFilters.exclude[InheritedNewAbstractMethodProblem](
    "fs2.Stream#LowPrioCompiler.fs2$Stream$LowPrioCompiler1$_setter_$idInstance_="
  ),
  ProblemFilters.exclude[InheritedNewAbstractMethodProblem](
    "fs2.Stream#LowPrioCompiler.idInstance"
  ),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Chunk.toArrayUnsafe"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Chunk#*.toArrayUnsafe"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.PullSyncInstance.attemptTap"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.PullSyncInstance.ifElseM"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.PullSyncInstance.fproductLeft"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Pull.free"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("fs2.Stream.free"),
  ProblemFilters.exclude[DirectMissingMethodProblem](
    "fs2.Stream#PartiallyAppliedFromBlockingIterator.apply$extension"
  ),
  ProblemFilters.exclude[DirectMissingMethodProblem](
    "fs2.Stream#PartiallyAppliedFromIterator.apply$extension"
  )
)

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS, io, reactiveStreams, benchmark, experimental)

lazy val IntegrationTest = config("it").extend(Test)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)
  )
  .settings(
    name := "fs2-core",
    sourceDirectories in (Compile, scalafmt) += baseDirectory.value / "../shared/src/main/scala",
    Compile / unmanagedSourceDirectories ++= {
      if (isDotty.value)
        List(CrossType.Pure, CrossType.Full).flatMap(
          _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + "-3"))
        )
      else Nil
    },
    // Libraries not yet cross-built for Dotty
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.1.1",
      "org.typelevel" %%% "cats-laws" % "2.1.1" % "test",
      "org.typelevel" %%% "cats-effect" % "2.1.4",
      "org.typelevel" %%% "cats-effect-laws" % "2.1.4" % "test"
    )
  )
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .settings(
    // Libraries cross-built for Dotty
    libraryDependencies ++= Seq(
      "org.scodec" %%% "scodec-bits" % "1.1.19",
      "org.typelevel" %%% "scalacheck-effect-munit" % "0.1.0" % "test",
      "org.typelevel" %%% "munit-cats-effect" % "0.3.0" % "test"
    )
  )

lazy val coreJVM = core.jvm
  .enablePlugins(SbtOsgi)
  .settings(
    Test / fork := true,
    OsgiKeys.exportPackage := Seq("fs2.*"),
    OsgiKeys.privatePackage := Seq(),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )

lazy val coreJS = core.js
  .disablePlugins(DoctestPlugin)
  .settings(
    scalaJSStage in Test := FastOptStage,
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )

lazy val io = project
  .in(file("io"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "fs2-io",
    Test / fork := true,
    Compile / unmanagedSourceDirectories ++= {
      if (isDotty.value)
        List(CrossType.Pure, CrossType.Full).flatMap(
          _.sharedSrcDir(baseDirectory.value / "io", "main").toList.map(f => file(f.getPath + "-3"))
        )
      else Nil
    },
    OsgiKeys.exportPackage := Seq("fs2.io.*"),
    OsgiKeys.privatePackage := Seq(),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(
        s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""",
        """fs2.*;version="${Bundle-Version}"""",
        "*"
      )
    },
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )
  .dependsOn(coreJVM % "compile->compile;test->test")

lazy val reactiveStreams = project
  .in(file("reactive-streams"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "fs2-reactive-streams",
    libraryDependencies ++= Seq(
      "org.reactivestreams" % "reactive-streams" % "1.0.3",
      "org.reactivestreams" % "reactive-streams-tck" % "1.0.3" % "test",
      ("org.scalatestplus" %% "testng-6-7" % "3.2.1.0" % "test").withDottyCompat(scalaVersion.value)
    ),
    OsgiKeys.exportPackage := Seq("fs2.interop.reactivestreams.*"),
    OsgiKeys.privatePackage := Seq(),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(
        s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""",
        """fs2.*;version="${Bundle-Version}"""",
        "*"
      )
    },
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )
  .dependsOn(coreJVM % "compile->compile;test->test")

lazy val benchmark = project
  .in(file("benchmark"))
  .enablePlugins(JmhPlugin)
  .settings(noPublishSettings)
  .settings(
    name := "fs2-benchmark",
    Test / run / javaOptions := (Test / run / javaOptions).value
      .filterNot(o => o.startsWith("-Xmx") || o.startsWith("-Xms")) ++ Seq("-Xms256m", "-Xmx256m")
  )
  .dependsOn(io)

lazy val microsite = project
  .in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .disablePlugins(MimaPlugin)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "fs2",
    micrositeDescription := "Purely functional, effectful, resource-safe, concurrent streams for Scala",
    micrositeGithubOwner := "functional-streams-for-scala",
    micrositeGithubRepo := "fs2",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    micrositeBaseUrl := "",
    micrositeHighlightTheme := "atom-one-light",
    micrositeExtraMdFilesOutput := resourceManaged.value / "main" / "jekyll",
    micrositeExtraMdFiles := Map(
      file("README.md") -> ExtraMdFileConfig(
        "index.md",
        "home",
        Map("title" -> "Home", "section" -> "home", "position" -> "0")
      )
    )
  )
  .settings(
    scalacOptions in Compile ~= {
      _.filterNot("-Ywarn-unused-import" == _)
        .filterNot("-Ywarn-unused" == _)
        .filterNot("-Xlint" == _)
        .filterNot("-Xfatal-warnings" == _)
    },
    scalacOptions in Compile += "-Ydelambdafy:inline",
    githubWorkflowArtifactUpload := false
  )
  .dependsOn(coreJVM, io, reactiveStreams)

lazy val experimental = project
  .in(file("experimental"))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "fs2-experimental",
    OsgiKeys.exportPackage := Seq("fs2.experimental.*"),
    OsgiKeys.privatePackage := Seq(),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(
        s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""",
        """fs2.*;version="${Bundle-Version}"""",
        "*"
      )
    },
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )
  .dependsOn(coreJVM % "compile->compile;test->test")
