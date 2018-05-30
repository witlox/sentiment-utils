lazy val root = (project in file(".")).
  settings(
    name := "sentiment-utils",
    version := "1.0",
    scalaVersion := "2.11.8",
    assemblyMergeStrategy in assembly := {
      case n if n.startsWith("META-INF/MANIFEST.MF") => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/resources" },
    coverageMinimum := 50,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    publishArtifact in Test := false
  )

val scalaTestVersion = "3.0.5"
val sparkVersion = "2.2.1"
val jodaTimeVersion = "2.9.9"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "joda-time" % "joda-time" % jodaTimeVersion
)