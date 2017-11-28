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

class InputStreamProcessSourceUnit extends UnitSpec {

  "Open" should {
    "read a file from resources" in {
      val process = new InputStreamProcessSource().get(getClass.getResourceAsStream("/processes/oct90001.json"))

      assert(process.id == "oct90001")
    }
  }

  "Process" should {

    "cope with a bigger process" in {
      val process = new InputStreamProcessSource().get(getClass.getResourceAsStream("/processes/oct90001.json"))

      val stanzas = process.stanzasForPath("/0/0")


      assert(stanzas.length == 1)
      assert(stanzas(0).text == 6)
    }

    "Give the right answer for multi value phrases" in {
      val process = new InputStreamProcessSource().get(getClass.getResourceAsStream("/processes/oct90001.json"))

      assert(process.getPhrase(0) == "Ask the customer if they have a tea bag")
      assert(process.getPhrase(0, webchat = true) == "Do you have a tea bag?")
    }
  }
}
