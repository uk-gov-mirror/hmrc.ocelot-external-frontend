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

package uk.gov.hmrc.ocelotexternalfrontend.controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Action
import uk.gov.hmrc.ocelotexternalfrontend.config.AppConfig
import uk.gov.hmrc.ocelotexternalfrontend.{ProcessParser, views}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class Ocelot @Inject()(val messagesApi: MessagesApi, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def ocelot(path: String) = Action.async {
    implicit request => {
      val process = new ProcessParser().parse(simpleProcess)

      Future.successful(Ok(views.html.ocelot(process, path)))
    }
  }



  private val simpleProcess =
    """
        {
         "meta":{"id":"oct90001", "title": "Test process"},
         "flow":{
           "start":{"type":"InstructionStanza","text":0,"next":["1"]},
           "1":{"type":"QuestionStanza","text":1,"answers":[2,3],"next":["2","3"]},
           "2":{"type":"InstructionStanza","text":4,"next":["end"]},
           "3":{"type":"InstructionStanza","text":5,"next":["end"]},
           "end":{"type":"EndStanza"}
         },
         "phrases":["Test process", "Is this a question", "Yes", "No", ["Internal", "External"], "You said no"]
        }
        """.stripMargin


}
