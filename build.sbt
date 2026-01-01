ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "3.3.7"

javaOptions += "--enable-native-access=ALL-UNNAMED"

lazy val root = (project in file("."))
  .settings(
    name             := "password-manager",
    idePackagePrefix := Some("org.aranadedoros"),
    libraryDependencies ++= Seq(
      "io.circe"      %% "circe-core"    % "0.14.15",
      "io.circe"      %% "circe-generic" % "0.14.15",
      "io.circe"      %% "circe-parser"  % "0.14.15",
      "de.mkammerer"   % "argon2-jvm"    % "2.12",
      "org.typelevel" %% "cats-effect"   % "3.5.4"
    )
  )
