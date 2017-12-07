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

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

import uk.gov.hmrc.play.test.UnitSpec

class PlaceholderManagerUnit extends UnitSpec {

  "Convert" should {
    "round trip, more or less" in {
      val html = PlaceholderManager.convert("Hello, world")
      assert(html.contentType == "text/html")
      assert(html.body == "Hello, world")
    }

    "Convert '[glossary:world]' to 'world'" in {
      val html = PlaceholderManager.convert("Hello, [glossary:world]")
      assert(html.contentType == "text/html")
      assert(html.body == "Hello, world")
    }

    "Convert two items" in {
      val html = PlaceholderManager.convert("[glossary:Hello], [glossary:world]")
      assert(html.contentType == "text/html")
      assert(html.body == "Hello, world")
    }

    "Convert links'" in {
      val html = PlaceholderManager.convert("Hello, [link:world:https://bing.com/]")
      assert(html.contentType == "text/html")
      assert(html.body == """Hello, <a href="https://bing.com/">world</a>""")
    }

    "handles glossary with id and text" in {
      val html = PlaceholderManager.convert("[glossary:Hello:World]")
      assert(html.contentType == "text/html")
      assert(html.body == "World")
    }

    "handles glossary with id and colons in text" in {
      val html = PlaceholderManager.convert("[glossary:Hello:World:!]")
      assert(html.contentType == "text/html")
      assert(html.body == "World:!")
    }

    "Handle broken placeholders: []" in {
      val html = PlaceholderManager.convert("Hello, []")
      assert(html.contentType == "text/html")
      assert(html.body == """Hello, []""")
    }

    "Handle broken placeholders: [unknown]" in {
      val html = PlaceholderManager.convert("Hello, [unknown]")
      assert(html.contentType == "text/html")
      assert(html.body == """Hello, [unknown]""")
    }

    "Handle broken placeholders: [unbalanced" in {
      val html = PlaceholderManager.convert("Hello, [unbalanced")
      assert(html.contentType == "text/html")
      assert(html.body == """Hello, [unbalanced""")
    }

    "Handle forwards timescales" in {
      val html = PlaceholderManager.convert("[timescale:2 weeks:date]")
      assert(html.contentType == "text/html")

      val today = LocalDate.now()
      val twoWeeks = today.plus(2, ChronoUnit.WEEKS)
      val twoWeekString = DateTimeFormatter.ofPattern("dd MMM YYYY").format(twoWeeks)
      assert(html.body == twoWeekString)
    }

    "Handle backwards timescales" in {
      val html = PlaceholderManager.convert("[timescale:2 weeks:date_ago]")
      assert(html.contentType == "text/html")

      val today = LocalDate.now()
      val twoWeeks = today.minus(2, ChronoUnit.WEEKS)
      val twoWeekString = DateTimeFormatter.ofPattern("dd MMM YYYY").format(twoWeeks)
      assert(html.body == twoWeekString)
    }

    "handle broken timescales" in {
      val html = PlaceholderManager.convert("[timescale:bogus:date_ago]")
      assert(html.contentType == "text/html")

      val today = LocalDate.now()
      val todayWeekString = DateTimeFormatter.ofPattern("dd MMM YYYY").format(today)
      assert(html.body == todayWeekString)

    }

  }
}
