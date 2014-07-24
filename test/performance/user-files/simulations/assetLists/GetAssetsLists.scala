package assetLists

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._

class GetAssetLists extends Simulation {
  val baseURL = System.getenv("BASE_URL")
  val httpConf = httpConfig
      .baseURL(baseURL)
      .acceptHeader("image/png,image/*;q=0.8,*/*;q=0.5")
      .acceptEncodingHeader("gzip, deflate")
      .acceptLanguageHeader("en-US,en;q=0.5")
      .connection("keep-alive")
      .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0")

  setUp(
    GetListApp.scn.users(1).ramp(15).protocolConfig(httpConf),
    GetListServers.scn.users(2).ramp(20).protocolConfig(httpConf),
    GetListDbs.scn.users(1).ramp(5).protocolConfig(httpConf),
    GetListStorages.scn.users(1).protocolConfig(httpConf) //.delay(15)
    )
}
