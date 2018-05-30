lazy val root = (project in file(".")).
  settings(
    name := "sentiment-utils",
    version := "1.0",
    scalaVersion := "2.11.8",
    mergeStrategy in assembly := {
      case PathList("org", "apache", xs @ _*) => MergeStrategy.last
      case PathList("com", "google", xs @ _*) => MergeStrategy.last
      case _ => MergeStrategy.first
    },
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/resources" },
    coverageMinimum := 50,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    publishArtifact in Test := false
  )

val configVersion = "1.3.0"
val sparkVersion = "2.1.0"
val jodaTimeVersion = "2.9.9"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % configVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion  % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "joda-time" % "joda-time" % jodaTimeVersion
)

lazy val defaultSettings = Defaults.coreDefaultSettings ++ Seq(
  resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)
