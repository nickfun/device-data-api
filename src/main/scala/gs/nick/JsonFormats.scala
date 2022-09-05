package gs.nick

import gs.nick.UserRegistry.ActionPerformed
import spray.json.RootJsonFormat

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat: RootJsonFormat[Users] = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

//  implicit val fmtDeviceReading = jsonFormat2(DeviceReading)
  implicit val fmtDeviceReading: RootJsonFormat[DeviceReading] = jsonFormat[String, Int, DeviceReading](DeviceReading, "timestamp", "count")
  implicit val fmtIncomingPayload = jsonFormat2(IncomingPayload)
}
//#json-formats
