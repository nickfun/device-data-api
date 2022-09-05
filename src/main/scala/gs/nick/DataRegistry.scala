package gs.nick

import java.time._
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

case class IncomingPayload(id: String, readings: List[DeviceReading])

case class DeviceResponse(currentCount: Long, mostRecentReading: DeviceReading)

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
             val knownRecords: ConcurrentHashMap[String, Boolean],
             val currentCount: AtomicLong,
             val recentReading: AtomicReference[DeviceReading]) {
  def accept(newReading: DeviceReading): Unit = {
    if (knownRecords.containsKey(newReading.timestamp)) {
      println(s"Previously Seen Record! $newReading")
    } else {
      println(s"new reading! $newReading")
      knownRecords.put(newReading.timestamp, true)
      currentCount.addAndGet(newReading.count);
      if (newReading.isAfter(recentReading.get())) {
        recentReading.set(newReading)
      }
    }
  }

  def toResponse(): DeviceResponse = {
    DeviceResponse(currentCount.get(), recentReading.get())
  }

  override def toString(): String =
    s"Device($id, knownRecordSize=${knownRecords.size()}, currentCount=${currentCount.get()}, recentReading=${recentReading.get()} "
}

class DataRegistry(val registry: ConcurrentHashMap[String, Device]) {

  def accept(payload: IncomingPayload): Unit = {
    if (registry.containsKey(payload.id)) {
      val device = registry.get(payload.id)
      println(s"known device ${payload.id}")
      payload.readings.foreach(data => device.accept(data))
    } else {
      if (payload.readings.nonEmpty) {
        println(s"new device! ${payload.id}")
        val first = payload.readings.head
        println(payload)
        val device = new Device(
          payload.id,
          new ConcurrentHashMap[String, Boolean](),
          new AtomicLong(first.count),
          new AtomicReference[DeviceReading](first)
        )
        payload.readings.tail.foreach(data => device.accept(data))
        registry.put(device.id, device)
      } else {
        println("empty list of readings")
      }
    }
  }
}
