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

import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.ocelotexternalfrontend.config.AppConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PingSpec extends UnitSpec with WithFakeApplication {
  val fakeRequest = FakeRequest("GET", "/")

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  val appConfig = new AppConfig(configuration, env)

  val controller = new Ping(messageApi, appConfig)

  "GET /" should {

    "return 200" in {
      val result = controller.ping(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }
}
