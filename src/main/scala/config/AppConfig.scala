package config

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration
import scala.util.Try


sealed trait AppConfig {
  val config = ConfigFactory.load()

  // Web Interface
  val webHost = config.getString("app.interface.web.host")
  val webPort = config.getInt("app.interface.web.port")

  // postgres

  val postgresDbHost  = config.getString("app.db.postgres.host")
  val postgresDbPort  = config.getInt("app.db.postgres.port")
  val postgresDbUser  = config.getString("app.db.postgres.user")
  val postgresDbPass  = config.getString("app.db.postgres.pass")
  val postgresDbName  = config.getString("app.db.postgres.name")

  val postgresDbPoolMaxObjects   = config.getInt("app.db.postgres.pool.max-objects")
  val postgresDbPoolMaxIdle      = config.getInt("app.db.postgres.pool.max-idle")
  val postgresDbPoolMaxQueueSize = config.getInt("app.db.postgres.pool.max-queue-size")

  //timeouts
  val httpRequestsTimeout   = Try(FiniteDuration(config.getInt("app.web.http-requests-timeout"),"seconds")).toOption.get
}

object AppConfig extends AppConfig
