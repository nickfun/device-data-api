package gs.nick

import java.time._
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

case class IncomingPayload(id: String, readings: List[DeviceReading])

case class DeviceReading(timestamp: String, count: Int) {
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  def isBefore(other: DeviceReading): Boolean = {
    val myself = Instant.from(dateFormatter.parse(timestamp))
    val compare = Instant.from(dateFormatter.parse(other.timestamp))
    myself.isBefore(compare)
  }

  def isAfter(other: DeviceReading): Boolean = {
    val myself = Instant.from(dateFormatter.parse(timestamp))
    val compare = Instant.from(dateFormatter.parse(other.timestamp))
    myself.isAfter(compare)
  }
}

class Device(val id: String,
             knownRecords: ConcurrentHashMap[String, Boolean],
             currentCount: AtomicLong,
             recentReading: AtomicReference[DeviceReading]) {
  def accept(newReading: DeviceReading): Unit = {
    if (knownRecords.containsKey(newReading.timestamp)) {
      println(s"Previously Seen Record! $newReading")
    } else {
      knownRecords.put(newReading.timestamp, true)
      currentCount.addAndGet(newReading.count);
      if (newReading.isAfter(recentReading.get())) {
        recentReading.set(newReading)
      }
    }
  }
}

class DataRegistry(registry: ConcurrentHashMap[String, Device]) {

  def accept(payload: IncomingPayload): Unit = {
    if (registry.containsKey(payload.id)) {
      val device = registry.get(payload.id)
      payload.readings.foreach(data => device.accept(data))
    } else {
      if (payload.readings.nonEmpty) {
        val first = payload.readings.head
        val device = new Device(
          payload.id,
          new ConcurrentHashMap[String, Boolean](),
          new AtomicLong(first.count),
          new AtomicReference[DeviceReading](first)
        )
        payload.readings.tail.foreach(data => device.accept(data))
        registry.put(device.id, device)
      }
    }
  }
}
