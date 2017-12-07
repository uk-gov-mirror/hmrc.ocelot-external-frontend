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

        val parts = in.substring(startIndex + 1, endIndex).split(":")

        if (parts.nonEmpty) {
          result.append(handlePlaceholder(parts))
        } else {
          result.append("[]")
        }

        inside = false
      } else if (!inside) {
        result.append(c)
      }
    }

    if (inside) {
      // We've reached the end of the input string but we still think
      // that we're inside a square bracket.
      // Dump the string beck to the output.
      result.append(in.substring(startIndex, in.length))
    }

    Html.apply(result.toString)
  }

  private def handlePlaceholder(parts: Seq[String]) = {
    val name = parts.head
    val args = parts.slice(1, parts.length)

    name match {
      case "glossary" => glossary(args)
      case "link" => link(args)
      case "timescale" => timescale(args)
      case _ => "[" + parts.mkString(":") + "]"
    }
  }

  private def glossary(parts: Seq[String]): String = if (parts.length > 1) parts.slice(1, parts.length).mkString(":") else parts.head

  private def link(parts: Seq[String]): String = a(href := parts.slice(1, parts.length).mkString(":"))(parts.head).toString

  private def timescale(parts: Seq[String]): String = {
    val tsPattern = "^(\\d+)\\s*(day|week)s?$".r
    val formatter = DateTimeFormatter.ofPattern("dd MMM YYYY")
    val format = parts(1)
    formatter.format(
      parts.head match {
        case tsPattern(num, duration) =>
          val dType = duration match {
            case "day" => ChronoUnit.DAYS
            case "week" => ChronoUnit.WEEKS
          }
          format match {
            case "date_ago" => LocalDate.now().minus(num.toLong, dType)
            case _ => LocalDate.now().plus(num.toLong, dType)
          }

        case _ => LocalDate.now()
      }
    )
  }
}
