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

  private val instructionJson = Json.parse("""{"id":"start", "type":"InstructionStanza", "next":["end"], "text": 0}""").as[JsObject]
  private val endJson = Json.parse("""{"id":"end", "type":"EndStanza"}""").as[JsObject]
  private val questionJson = Json.parse("""{"type":"QuestionStanza","text":1,"answers":[2,3],"next":["2","3"]}""").as[JsObject]

  "An Instruction Stanza" should {
    "Know it's text, next" in {
      val stanza = new InstructionStanza(instructionJson)

      assert(stanza.kind == "instruction")
      assert(stanza.text == 0)
      assert(stanza.next.length == 1)
      assert(stanza.next.head == "end")
    }
  }

  "An End stanza" should {
    "Parse" in {
      new EndStanza(endJson)
    }
  }

  "A Question stanza" should {
    "know about answers" in {
      val stanza = new QuestionStanza(questionJson)
      assert(stanza.kind == "question")
      assert(stanza.next.length == 2)
      assert(stanza.answers.length == 2)
      assert(stanza.answer(0) == 2)
    }
  }

}
