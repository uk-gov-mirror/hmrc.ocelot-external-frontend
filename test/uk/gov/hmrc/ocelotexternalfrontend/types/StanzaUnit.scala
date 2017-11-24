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

package uk.gov.hmrc.ocelotexternalfrontend.types

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.test.UnitSpec

class StanzaUnit extends UnitSpec {

  val instructionJson = Json.parse("""{"id":"start", "type":"InstructionStanza", "next":["end"], "text": 0}""").as[JsObject]
  val endJson = Json.parse("""{"id":"end", "type":"EndStanza"}""").as[JsObject]


  "An Instruction Stanza" should {
    "Know it's text, next" in {
      val stanza = new InstructionStanza(instructionJson)

      assert(stanza.kind == "instruction")
      assert(stanza.text == 0)
      assert(stanza.next.length == 1)
      assert(stanza.next(0) == "end")
    }
  }

  "An End stanza" should {
    "Parse" in {
      new EndStanza(endJson)
    }
  }

}
