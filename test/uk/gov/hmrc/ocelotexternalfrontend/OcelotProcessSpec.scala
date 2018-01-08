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

package uk.gov.hmrc.ocelotexternalfrontend

import play.twirl.api.Html
import uk.gov.hmrc.play.test.UnitSpec

class OcelotProcessSpec extends UnitSpec {

  private val simpleProcess =
    """
        {
         "meta":{"id":"oct90001", "title":"Test process"},
         "flow":{
           "start":{"type":"InstructionStanza","text":0,"next":["1"]},
           "1":{"type":"QuestionStanza","text":1,"answers":[2,3],"next":["2","3"]},
           "2":{"type":"InstructionStanza","text":4,"next":["end"], "link":0},
           "3":{"type":"InstructionStanza","text":5,"next":["end"]},
           "end":{"type":"EndStanza"}
         },
         "phrases":[["Test process", "External text"], "Is this a question", "Yes", "No", ["Internal", "External"], "Hello, [glossary:world]"],
         "links":[
          {
          "dest": "https://gov.uk/",
           "title": "test link",
           "window": true,
           "leftbar": true,
           "id": 0
         }
         ]
        }
        """.stripMargin

  "A process" should {

    val process = new StringProcessSource().parse(simpleProcess)
    "know it's id" in {
      assert(process.id == "oct90001")
    }

    "have a bunch of stanzas" in {
      assert(process.getAllStanzas.size == 5)

      assert(process.getStanza("start").kind == "instruction")
      assert(process.getStanza("end").kind == "end")
    }

    "have a phrasebank" in {
      assert(process.phrases.lengthCompare(6) == 0)
      assert(process.getPhrase(0) == "Test process")
      assert(process.getPhrase(4) == "Internal")
    }

    "Get stanzas for a path" in {
      val path = "/"
      val stanzas = process.stanzasForPath(path)

      assert(stanzas.lengthCompare(2) == 0)
      assert(stanzas(0).id == "start")
      assert(stanzas(1).id == "1")
    }

    "Get stanzas for a longer path (left)" in {
      val path = "/0"
      val stanzas = process.stanzasForPath(path)

      assert(stanzas.lengthCompare(2) == 0)
      assert(stanzas(0).id == "2")
      assert(stanzas(1).id == "end")
    }

    "Get stanzas for a longer path (right)" in {
      val path = "/1"
      val stanzas = process.stanzasForPath(path)

      assert(stanzas.lengthCompare(2) == 0)
      assert(stanzas(0).id == "3")
      assert(stanzas(1).id == "end")
    }

    "Get text for a stanza" in {
      val stanza = process.getStanza("start")
      assert(process.getInternalText(stanza) == "Test process")
    }

    "Get webchat text for a stanza" in {
      val stanza = process.getStanza("start")
      assert(process.getExternalText(stanza) == "External text")
    }

    "Get text for a stanza, even when we ask for external" in {
      val stanza = process.getStanza("1")
      assert(process.getExternalText(stanza) == "Is this a question")
    }

    "Get HTML for a stanza" in {
      val stanza = process.getStanza("1")
      var html = process.getInternalHTML(stanza)
      assert(html.contentType == "text/html")
      assert(html.body == "Is this a question")
    }

    "Placeholders work" in {
      val stanza = process.getStanza("3")
      var html = process.getInternalHTML(stanza)
      assert(html.contentType == "text/html")
      assert(html.body == "Hello, world")
    }

    "know its links" in {
      val links = process.links

      assert(links.size == 1)
      assert(links(0).href == "https://gov.uk/")
      assert(links(0).title == "test link")

    }

    "wrap a link" in {

      val stanza = process.getStanza("2")
      val html = process.getExternalHTML(stanza)

      assert(html.body == """<a href="https://gov.uk/">External</a>""")
    }

  }
}
