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

package uk.gov.hmrc.ocelotexternalfrontend.controllers

import javax.inject.Inject

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.ocelotexternalfrontend.config.AppConfig
import uk.gov.hmrc.ocelotexternalfrontend.types.ExternalLinkStanza
import uk.gov.hmrc.ocelotexternalfrontend.{InputStreamProcessSource, OcelotProcess, views}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class Ocelot @Inject()(val messagesApi: MessagesApi, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private val log: Logger = Logger(this.getClass)

  def ocelotBase(id: String, q: Option[String]): Action[AnyContent] = ocelot(id, "/", q)

  def ocelot(id: String, path: String, q: Option[String]): Action[AnyContent] = Action.async {
    implicit request => {

      if (id.matches("^[a-z]{3}\\d{5}$")) {
        implicit val process: OcelotProcess = new InputStreamProcessSource().get(getClass.getResourceAsStream("/processes/" + id + ".json"))
        var targetPath = path

        if (q.isDefined) {
          if (targetPath.last != '/') {
            targetPath += "/"
          }
          targetPath += q.get
        }

        val stanzas = process.stanzasForPath(targetPath)

        if (stanzas.length == 1 && stanzas.head.kind == "externalLink") {
          log.info(s"")
          val extLink: ExternalLinkStanza = stanzas.head.asInstanceOf[ExternalLinkStanza]
          Future.successful(Redirect(extLink.href, FOUND))
        } else {
          log.info(s"Handling request for $targetPath")
          Future.successful(Ok(views.html.ocelot(stanzas, targetPath, id)))
        }
      } else {
        Future.successful(NotFound(views.html.error_template("Process not found", "Process not found", "We could not find the process you're looking for")))
      }
    }
  }
}
