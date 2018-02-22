/*
 * Copyright 2018 HM Revenue & Customs
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
  private val importantJson = Json.parse("""{"type":"ImportantStanza","text":1,"next":["1"]}""").as[JsObject]
  private val linkJson = Json.parse("""{"type":"ImportantStanza","link":1,"text":1,"next":["1"]}""").as[JsObject]

  "An Instruction Stanza" should {
    "Know it's text, next" in {
      val stanza = new InstructionStanza("test", instructionJson)

      assert(stanza.kind == "instruction")
      assert(stanza.text == 0)
      assert(stanza.next.length == 1)
      assert(stanza.next.head == "end")
    }
  }

  "An End stanza" should {
    "Parse" in {
      new EndStanza("test", endJson)
    }
  }

  "A Question stanza" should {
    "know about answers" in {
      val stanza = new QuestionStanza("test", questionJson)
      assert(stanza.kind == "question")
      assert(stanza.next.length == 2)
      assert(stanza.answers.length == 2)
      assert(stanza.answer(0) == 2)
    }

    "get an answer index from phrase id" in {
      val stanza = new QuestionStanza("test", questionJson)

      stanza.getAnswerById(2) shouldBe 0
      stanza.getAnswerById(1) shouldBe -1
    }
  }

  "An instruction stanza" should {
      "really be a callout" in {
        val stanza = new CalloutStanza("test", importantJson)
        assert(stanza.kind == "callout")
        assert(stanza.subkind == "important")
      }
  }

  "A stanza with a link" should {
    "know its link id" in {
      val stanza = new CalloutStanza("test", linkJson)
      assert(stanza.link.isDefined)
      assert(stanza.link.get == 1)
      assert(stanza.hasLink)
    }
    "not have a link in some circumstances" in {
      val stanza = new CalloutStanza("test", importantJson)
      assert(stanza.link.isEmpty)
      assert(!stanza.hasLink)
    }
  }

  "An external link stanza" should {
    "know its destination" in {
      val stanza = new ExternalLinkStanza("test", "https://gov.uk/")
      assert(stanza.kind == "externalLink")
      assert(stanza.href == "https://gov.uk/")
    }
  }
}
