package gs.nick

import spray.json.RootJsonFormat

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._


  implicit val fmtDeviceReading = jsonFormat[String, Int, DeviceReading](DeviceReading, "timestamp", "count")
  implicit val fmtIncomingPayload = jsonFormat2(IncomingPayload)
  implicit val fmtDeviceResponse = jsonFormat2(DeviceResponse)
}
//#json-formats
