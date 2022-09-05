package gs.nick


import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

class DataRegistrySpec extends AnyWordSpec with Matchers with ScalaFutures {

  "DeviceReading" should {
    "be before/after another" in {
      val e1 = DeviceReading("1984-09-29T16:08:15+01:00", 10)
      val e2 = DeviceReading("2020-09-29T16:08:15+01:00", 4)

      e1.isBefore(e2) shouldBe true
      e2.isAfter(e1) shouldBe true
    }
  }

  "Device" should {

    def newDevice(): Device = {
      new Device(
        "test-device",
        new ConcurrentHashMap[String, Boolean](),
        new AtomicLong(0),
        new AtomicReference[DeviceReading](DeviceReading("1900-09-29T16:08:15+01:00", 0)))
    }

    "accept new data" in {
      val device = newDevice()
      device.accept(DeviceReading("2000-09-29T16:08:15+01:00", 4))
      device.currentCount.get() shouldBe 4
      device.accept(DeviceReading("2020-09-29T16:08:15+01:00", 4))
      device.currentCount.get() shouldBe 8
    }

    "ignore seen data" in {
      val e1 = DeviceReading("1984-09-29T16:08:15+01:00", 10)
      val e2 = DeviceReading("2020-09-29T16:08:15+01:00", 4)
      val dataWithDuplicates = List(e1, e1, e1, e2, e2, e2)
      val device = newDevice()
      dataWithDuplicates.foreach { data =>
        device.accept(data)
      }
      device.currentCount.get() shouldEqual (e1.count + e2.count)
    }

    "keep most recent record available" in {
      val e1 = DeviceReading("1984-09-29T16:08:15+01:00", 10)
      val e2 = DeviceReading("2020-09-29T16:08:15+01:00", 4)
      val e3 = DeviceReading("2030-09-29T16:08:15+01:00", 4)
      val device = newDevice()
      device.accept(e1)
      device.recentReading.get() shouldEqual e1

      device.accept(e2)
      device.recentReading.get() shouldEqual e2

      device.accept(e1)
      device.recentReading.get() shouldEqual e2

      device.accept(e3)
      device.recentReading.get() shouldEqual e3
    }
  }

}
