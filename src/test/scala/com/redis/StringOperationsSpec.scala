package com.redis

import scala.concurrent.Future

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import api.RedisOps._
import serialization._
import StringCommands._


@RunWith(classOf[JUnitRunner])
class StringOperationsSpec extends RedisSpecBase {

  describe("set") {
    it("should set values to keys") {
      val numKeys = 3
      val (keys, values) = (1 to numKeys map { num => ("key" + num, "value" + num) }).unzip
      val writes = keys zip values map { case (key, value) => set(key, value) apply client }

      writes foreach { _.futureValue should be (true) }
    }

    it("should not set values to keys already existing with option NX") {
      val key = "key100"
      (set(key, "value100") apply client).futureValue should be (true)

      val v = set(key, "value200", Some(NX)) apply client 
      v.futureValue match {
        case true => fail("an existing key with an value should not be set with NX option")
        case false => (get(key) apply client).futureValue should equal (Some("value100"))
      }
    }

    it("should not set values to non-existing keys with option XX") {
      val key = "value200"
      val v = set(key, "value200", Some(XX)) apply client 
      v.futureValue match {
        case true => fail("set on a non existing key with XX will fail")
        case false => (get(key) apply client).futureValue should equal (None)
      }
    }
  }
}
