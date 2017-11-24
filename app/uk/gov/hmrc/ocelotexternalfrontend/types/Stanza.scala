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

import play.api.libs.json.{JsArray, JsObject}

abstract class Stanza(json: JsObject) {

  val kind : String
}

class EndStanza(json: JsObject) extends Stanza(json) {

  val kind = "end"
}

class InstructionStanza(json: JsObject) extends Stanza(json) {

  val kind = "instruction"
  val next : Seq[String] = (json \ "next").as[List[String]]
  val text: Int = (json \ "text").as[Int]

}