package gs.nick

import gs.nick.UserRegistry.ActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

  implicit val fmtDeviceReading = jsonFormat2(DeviceReading)
  implicit val fmtIncomingPayload = jsonFormat2(IncomingPayload)
}
//#json-formats
