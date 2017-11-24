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

import uk.gov.hmrc.play.test.UnitSpec

class ProcessParserSpec extends UnitSpec {

  private val simpleProcess =
    """
        {
         "meta":{"id":"oct90001"},
         "flow":{
           "start":{"id":"start","type":"InstructionStanza","text":0,"next":["end"]},
           "end":{"type":"EndStanza"}
         },
         "phrases":["World's shortest process"]
        }
        """.stripMargin

  "A Parser" should {

    val process = new ProcessParser().parse(simpleProcess)
    "know it's id" in {
      assert(process.id == "oct90001")
    }

    "have a bunch of stanzas" in {
      assert(process.stanzas.size == 2)

      assert(process.stanza("start").kind == "instruction")
      assert(process.stanza("end").kind == "end")
    }

    "have a phrasebank" in {
      assert(process.phrases.length == 1)
      assert(process.phrase(0) == "World's shortest process")
    }
  }
}
