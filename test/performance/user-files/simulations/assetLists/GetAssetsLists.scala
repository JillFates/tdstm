package assetLists

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class GetAssetLists extends Simulation {
  val baseURL = System.getenv("BASE_URL")
  val httpConf = http
      .baseURL(baseURL)
      .acceptHeader("image/png,image/*;q=0.8,*/*;q=0.5")
      .acceptEncodingHeader("gzip, deflate")
      .acceptLanguageHeader("en-US,en;q=0.5")
      .connection("keep-alive")
      .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0")

  setUp(
    GetListApp.scn.inject(rampUsers(1) over (1 seconds)),
    GetListServers.scn.inject(rampUsers(1) over (20 seconds)),
    GetListDbs.scn.inject(rampUsers(1) over (5 seconds)),
    GetListStorages.scn.inject(rampUsers(1) over (15 seconds))
    ).protocols(httpConf)
}
