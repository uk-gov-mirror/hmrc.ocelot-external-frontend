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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.{charset, contentType, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.ocelotexternalfrontend.config.AppConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class OcelotSpec extends UnitSpec with WithFakeApplication {
  val fakeRequest = FakeRequest("GET", "/")

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  val appConfig = new AppConfig(configuration, env)

  val controller = new Ocelot(messageApi, appConfig)

  "GET /" should {

    "return 200" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "Show the title" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))
      assert(html.select(".page-title").text() == "Customer wants to make a cup of tea")
    }

    "show a question" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))
      assert(html.select(".question .prompt").text() == "Ask the customer if they have a tea bag")
    }

    "show a form" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))
      assert(html.getElementsByTag("form").attr("action") == "/ocelot-external-frontend/ocelot/")
    }

    "Give radios the right values" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val inputs = html.select("input[type=radio]")
      assert(inputs.size() == 2)
      assert(inputs.get(0).attr("value") == "0")
      assert(inputs.get(1).attr("value") == "1")

      val labels = html.select(".question label")
      assert(labels.size() == 2)
      assert(labels.get(0).text() == "Yes - they do have a tea bag")
      assert(labels.get(1).text() == "No - they do not have a tea bag")
    }

    "include a link back to the start" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val home = html.select(".home")
      assert(home.size == 1)
      assert(home.get(0).text == "Back to start")
    }

  }

  "GET /0" should {
    "render another question" in {
      val result = controller.ocelot("/0", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      assert(html.select(".question .prompt").text() == "Ask the customer if they have a cup")
    }
  }

  "GET /?q=0" should {

    "render the same as /0" in {
      val result = controller.ocelot("/0", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      assert(html.select(".question .prompt") .text() == "Ask the customer if they have a cup")
    }
  }

  "GET /1" should {
    "end the process" in {

      val result = controller.ocelot("/1", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val instructions = html.select(".instruction")
      assert(instructions.size() == 2)

      val terminal = html.select(".terminal")
      assert(terminal.size() == 1)
    }

    "include a link back to the start" in {
      val result = controller.ocelot("/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val home = html.select(".home")
      assert(home.size == 1)
      assert(home.get(0).text == "Back to start")
    }
  }

  "GET /2" should {
    "fail gracefully" in {
      val result = controller.ocelot("/2", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val terminal = html.select(".terminal")
      assert(terminal.size() == 1)

    }
  }

}