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

import play.api.libs.json.JsObject

abstract class Stanza(idStr: String, json: JsObject) {
  val id: String = idStr
  val text: Int = -1
  val kind: String
  val next: Seq[String] = List[String]()
  def isQuestion: Boolean = false

  def isTerminal: Boolean = next.isEmpty
}

class EndStanza(id: String, json: JsObject) extends Stanza(id, json) {
  override val isTerminal = true
  val kind = "end"
}

class InstructionStanza(id: String, json: JsObject) extends Stanza(id, json) {
  override val text: Int = (json \ "text").as[Int]
  val kind = "instruction"
  override val next: Seq[String] = (json \ "next").as[List[String]]
}

class QuestionStanza(id: String, json: JsObject) extends Stanza(id, json) {
  override val isQuestion: Boolean = true
  override val text: Int = (json \ "text").as[Int]
  val kind = "question"
  override val next: Seq[String] = (json \ "next").as[List[String]]
  val answers: Seq[Int] = (json \ "answers").as[List[Int]]

  def answer(id: Int): Int = answers(id)
}

class CalloutStanza(id: String, json: JsObject) extends InstructionStanza(id, json) {
  override val kind = "callout"
  val subkind: String = {
    val t = (json \ "type").as[String]
    t.substring(0, t.length - "Stanza".length).toLowerCase
  }
}
