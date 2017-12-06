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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import play.api.Logger
import play.twirl.api.Html

import scalatags.Text.all._

object PlaceholderManager {

  private val log = Logger(PlaceholderManager.getClass)

  def convert(in: String): Html = {
    val result = new StringBuilder()
    var startIndex = 0
    var inside = false
    var endIndex = 0

    for ((c, i) <- in.zipWithIndex) {
      if (c == '[') {
        startIndex = i
        inside = true
      } else if (c == ']') {
        endIndex = i

        val raw = in.substring(startIndex + 1, endIndex)

        val parts = raw.split(":")

        if (parts.nonEmpty) {
          val name = parts.head
          val args = parts.slice(1, parts.length)

          result.append(name match {
            case "glossary" => glossary(args)
            case "link" => link(args)
            case "timescale" => timescale(args)
            case _ => "[" + parts.mkString(":") + "]"
          })
        } else {
          result.append("[]")
        }

        inside = false
      } else if (!inside) {
        result.append(c)
      }
    }

    if (inside) {
      // Unbalanced '['
      result.append(in.substring(startIndex, in.length))
    }

    Html.apply(result.toString)
  }

  def glossary(parts: Seq[String]): String = if (parts.length > 1) parts.slice(1, parts.length).mkString(":") else parts.head

  def link(parts: Seq[String]): String = a(href := parts.slice(1, parts.length).mkString(":"))(parts.head).toString

  def timescale(parts: Seq[String]): String = {
    val tsPattern = "^(\\d+)\\s*(day|week)s?$".r

    DateTimeFormatter.ofPattern("dd MMM YYYY").format(parts.head match {
      case tsPattern(num, duration) =>
        duration match {
          case "day" => LocalDate.now().plus(num.toLong, ChronoUnit.DAYS)
          case "week" => LocalDate.now().plus(num.toLong, ChronoUnit.WEEKS)
        }
      case _ => LocalDate.now()
    })
  }

}
