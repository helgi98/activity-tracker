package org.helgi.activity.config

import pureconfig._
import pureconfig.generic.auto._

case class DbConfig(driver: String,
                    url: String,
                    user: String,
                    password: String,
                    pool: Int)

case class ServerConfig(host: String, port: Int)

case class AuthConfig(url: String)

case class AppConfig(server: ServerConfig, db: DbConfig, auth: AuthConfig)
