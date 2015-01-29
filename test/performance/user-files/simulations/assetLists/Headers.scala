package assetLists

object Headers {
//login

  val headers_0 = Map("Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")

  //SingIn
  val headers_3 = Map(
      "Content-Type" -> """application/x-www-form-urlencoded"""
  )

  //Update last page Get List Servers
  val headers_2 = Map(
      "Accept" -> """text/javascript, text/html, application/xml, text/xml, */*""",
      "Cache-Control" -> """no-cache""",
      "Content-Type" -> """application/x-www-form-urlencoded; charset=UTF-8""",
      "Pragma" -> """no-cache""",
      "X-Prototype-Version" -> """1.6.0""",
      "X-Requested-With" -> """XMLHttpRequest"""
  )
//  get list app 
  val headers_4 = Map(
    "Accept" -> """text/javascript, text/html, application/xml, text/xml, */*""",
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/x-www-form-urlencoded; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Prototype-Version" -> """1.6.0""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )
//asset entity list json get list of servers
  val headers_5 = Map(
      "Accept" -> """application/json, text/javascript, */*; q=0.01""",
      "X-Requested-With" -> """XMLHttpRequest"""
  )
// get list of app
   val headers_7 = Map(
      "Accept" -> """application/json, text/javascript, */*; q=0.01""",
      "X-Requested-With" -> """XMLHttpRequest"""
  )
   
  val headers_9 = Map(
    "Accept" -> """*/*""",
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/x-www-form-urlencoded; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )
 
}