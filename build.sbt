name := "zen-conf"

version := "1.0-SNAPSHOT"

resolvers ++= Seq(
  "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
  "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
)

libraryDependencies ++= Seq(
  "org.mandubian" %% "play-actor-room" % "0.2"
)

play.Project.playScalaSettings
