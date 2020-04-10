name := "zio"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaHttpVersion = "10.1.7"

libraryDependencies ++= Seq (
  "dev.zio" %% "zio" % "1.0.0-RC18-2",
  "com.typesafe"           % "config"                % "1.3.4",
  "com.github.mauricio"%% "mysql-async"% "0.2.21",
  "com.typesafe.akka"      %% "akka-http"            % "10.1.7",
  "com.typesafe.akka"      %% "akka-stream"          % "2.5.19",
  "com.typesafe.akka"      %% "akka-http-testkit"    % akkaHttpVersion   % Test,
  "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion
)


