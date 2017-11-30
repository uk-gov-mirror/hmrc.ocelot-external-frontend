/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.ocelotexternalfrontend

import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import uk.gov.hmrc.ocelotexternalfrontend.types._

import scala.collection.mutable.ListBuffer

class OcelotProcess(json: JsObject) {

  val title: String = (json \ "meta" \ "title").as[String]
  val id: String = (json \ "meta" \ "id").as[String]

  val flow: Map[String, Stanza] = (for (f <- (json \ "flow").as[JsObject].fields)
    yield {
      val id = f._1
      val raw = f._2
      val kind = (raw \ "type").as[String]
      val stanza = kind match {
        case "InstructionStanza" => new InstructionStanza(id, raw.as[JsObject])
        case "EndStanza" => new EndStanza(id, raw.as[JsObject])
        case "QuestionStanza" => new QuestionStanza(id, raw.as[JsObject])
        case "ImportantStanza" => new CalloutStanza(id, raw.as[JsObject])
        case _ => throw new IllegalArgumentException("Unknown stanza type: " + kind)
      }
      id -> stanza
    }).toMap

  val phrases: Seq[Seq[String]] = for (v <- (json \ "phrases").as[List[JsValue]])
    yield v match {
      case _: JsString => Vector[String](v.as[String])
      case _: JsArray => v.as[Vector[String]]
      case _: Any => throw new IllegalArgumentException("Unexpected json type")
    }

  private val log: Logger = Logger(this.getClass)

  // Parse the flow into a bunch of Stanza types

  def stanzasForPath(path: String): Seq[Stanza] = {
    val parts = for(s <- path.split("/") if s.matches("^\\d+$"))
      yield s.toInt

    var index = 0

    var result = ListBuffer[Stanza]()

    var stanza = getStanza("start")

    while (true) {
      result += stanza
      if (stanza.next.isEmpty) {
        // If we've got nowhere left to go, then return what we've got
        return result
      } else if (stanza.next.length == 1) {
        // Exactly one route out. Get the next stanza
        stanza = getStanza(stanza.next.head)
      } else {
        if (index < parts.length) {
          // Still working through the input from the user

          if (parts(index) >= 0 && parts(index) < stanza.next.length) {
            // Valid answer. Get the next stanza
            stanza = getStanza(stanza.next(parts(index)))
            result.clear()
            index += 1
          } else {
            // Invalid answer. Probably someone trying to be naughty
            log.info(s"Request for [$path] failed, unknown answer at index [$index]")
            result.clear()
            result += getStanza("end")
            return result
          }
        } else {
          return result
        }
      }
    }
    log.error("Dropped out of the bottom of while(true)")
    throw new IllegalStateException("Should not be able to get here")
  }

  def getStanza(id: String): Stanza = flow(id)

  def getInternalText(stanza: Stanza): String = getPhrase(stanza.text)

  def getExternalText(stanza: Stanza): String = getPhrase(stanza.text, webchat = true)

  def getPhrase(id: Int, webchat: Boolean = false): String = if (webchat) phrases(id).last else phrases(id).head

  def getAllStanzas: Map[String, Stanza] = flow

}
