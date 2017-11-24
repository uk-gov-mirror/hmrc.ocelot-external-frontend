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

import java.util

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import uk.gov.hmrc.ocelotexternalfrontend.types.{EndStanza, InstructionStanza, QuestionStanza, Stanza}

class OcelotProcess(json: JsObject) {

  val id: String = (json \ "meta" \ "id").as[String]

  val flow: util.HashMap[String, Stanza] = new util.HashMap[String, Stanza]()
  (json \ "flow").as[JsObject].fields.foreach { key =>
    flow.put(key._1, (key._2 \ "type").as[String] match {
      case "InstructionStanza" => new InstructionStanza(key._2.as[JsObject])
      case "EndStanza" => new EndStanza(key._2.as[JsObject])
      case "QuestionStanza" => new QuestionStanza(key._2.as[JsObject])
      case _ => throw new IllegalArgumentException("Unknown stanza type")
    })
  }

  val phrases: Seq[Seq[String]] = {
    var result = Vector[Vector[String]]()

    (json \ "phrases").as[List[JsValue]].foreach{v => {
      v match {
        case s: JsString => result =  result :+ Vector[String](v.as[String])
        case a: JsArray => result = result :+ v.as[Vector[String]]
        case _: Any => throw new IllegalArgumentException("Unexpected json type")
      }
    }}
    result
  }

  def phrase(id: Int): String = phrases(id)(0)

  def stanza(id: String): Stanza = flow.get(id)
  def stanzas: util.HashMap[String, Stanza] = flow
}
