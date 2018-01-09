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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import uk.gov.hmrc.play.test.UnitSpec

class PlaceholderManagerUnit extends UnitSpec {

  "Convert" should {
    "round trip, more or less" in {
      val html = PlaceholderManager.convert("Hello, world")

      assert(html.toString == "Hello, world")
    }

    "Convert '[glossary:world]' to 'world'" in {
      val html = PlaceholderManager.convert("Hello, [glossary:world]")

      assert(html.toString == "Hello, world")
    }

    "Convert two items" in {
      val html = PlaceholderManager.convert("[glossary:Hello], [glossary:world]")

      assert(html.toString == "Hello, world")
    }

    "Convert links'" in {
      val html = PlaceholderManager.convert("Hello, [link:world:https://bing.com/]")

      assert(html.toString == """Hello, <a href="https://bing.com/">world</a>""")
    }

    "handles glossary with id and text" in {
      val html = PlaceholderManager.convert("[glossary:Hello:World]")

      assert(html.toString == "World")
    }

    "handles glossary with id and colons in text" in {
      val html = PlaceholderManager.convert("[glossary:Hello:World:!]")

      assert(html.toString == "World:!")
    }

    "Handle broken placeholders: []" in {
      val html = PlaceholderManager.convert("Hello, []")
      assert(html.toString == """Hello, []""")
    }

    "Handle broken placeholders: [unknown]" in {
      val html = PlaceholderManager.convert("Hello, [unknown]")

      assert(html.toString == """Hello, [unknown]""")
    }

    "Handle broken placeholders: [unbalanced" in {
      val html = PlaceholderManager.convert("Hello, [unbalanced")

      assert(html.toString == """Hello, [unbalanced""")
    }

    "Handle forwards timescales" in {
      val html = PlaceholderManager.convert("[timescale:2 weeks:date]")

      val today = LocalDate.now()
      val twoWeeks = today.plus(2, ChronoUnit.WEEKS)
      val twoWeekString = PlaceholderManager.formatter.format(twoWeeks)
      assert(html.toString == twoWeekString)
    }

    "Handle backwards timescales" in {
      val html = PlaceholderManager.convert("[timescale:2 weeks:date_ago]")

      val today = LocalDate.now()
      val twoWeeks = today.minus(2, ChronoUnit.WEEKS)
      val twoWeekString = PlaceholderManager.formatter.format(twoWeeks)
      assert(html.toString == twoWeekString)
    }

    "handle broken timescales" in {
      val html = PlaceholderManager.convert("[timescale:bogus:date_ago]")

      val today = LocalDate.now()
      val todayWeekString = PlaceholderManager.formatter.format(today)
      assert(html.toString == todayWeekString)

    }
  }
}
