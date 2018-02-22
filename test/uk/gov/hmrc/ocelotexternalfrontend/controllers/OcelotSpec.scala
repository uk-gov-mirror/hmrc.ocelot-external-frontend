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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Messages, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.{charset, contentType, _}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.ocelotexternalfrontend.config.AppConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class OcelotSpec extends UnitSpec with WithFakeApplication {

  val log = Logger(getClass)

  val fakeRequest = FakeRequest("GET", "/ocelot9001")

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  val messageApi: MessagesApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))

  implicit val messages: Messages = messageApi.preferred(fakeRequest)

  val appConfig = new AppConfig(configuration, env)

  val controller = new Ocelot(messageApi, appConfig)

  "GET /" should {
    "return 404" in {
      val result = controller.ocelot("", "/", None).apply(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "GET /oct90001" should {

    "return 200" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "Show the title" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))
      assert(html.select(".header__menu__proposition-name").text() == "Customer wants to make a cup of tea")
    }

    "show a question" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))
      assert(html.select(".question .prompt").text() == "Do you have a tea bag?")
    }

    "show a form" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))
      assert(html.getElementsByTag("form").attr("action") == "/ocelot-external-frontend/oct90001/")
    }

    "Give radios the right values" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val inputs = html.select("input[type=radio]")
      assert(inputs.size() == 2)
      assert(inputs.get(0).attr("value") == "yes-they-do-have-a-tea-bag")
      assert(inputs.get(1).attr("value") == "no-they-do-not-have-a-tea-bag")

      val labels = html.select(".question label")
      assert(labels.size() == 2)
      assert(labels.get(0).text() == "Yes - they do have a tea bag")
      assert(labels.get(1).text() == "No - they do not have a tea bag")
    }

    "not include a link back to the start" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val home = html.select(".home")
      assert(home.size == 0)
    }

    "set the title" in {
      val result = controller.ocelot("oct90001", "/", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val title = html.head.getElementsByTag("title")

      assert(title.size == 1)
      assert(title.get(0).text == "Do you have a tea bag? " + messages("general.endash") + " " + messages("title.suffix"))
    }
  }

  "GET /yes-they-do-have-a-tea-bag" should {
    "render another question" in {
      val result = controller.ocelot("oct90001", "/yes-they-do-have-a-tea-bag", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      assert(html.select(".question .prompt").text() == "Do you have a cup?")
    }

    "render the correct back link" in {
      val result = controller.ocelot("oct90001", "/yes-they-do-have-a-tea-bag", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val back = html.select(".link-back")

      assert(back.size() == 1)
      assert(back.get(0).attr("href") == "/ocelot-external-frontend/oct90001/")
    }
  }

  "GET /?q=yes-they-do-have-a-tea-bag" should {

    "render the same as /yes-they-do-have-a-tea-bag" in {
      val result = controller.ocelot("oct90001", "/", Some("yes-they-do-have-a-tea-bag")).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      assert(html.select(".question .prompt").text() == "Do you have a cup?")
    }
  }

  "GET /no-they-do-not-have-a-tea-bag" should {
    "end the process" in {

      val result = controller.ocelot("oct90001", "/no-they-do-not-have-a-tea-bag", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val instructions = html.select(".instruction")
      assert(instructions.size() == 2)

      val terminal = html.select(".terminal")
      assert(terminal.size() == 1)
    }

    "include a link back to the start" in {
      val result = controller.ocelot("oct90001", "/no-they-do-not-have-a-tea-bag", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val home = html.select(".home")
      assert(home.size == 1)
      assert(home.get(0).text == Messages("link.backToStart"))
    }
  }

  "GET /maybe-we-ve-got-a-teabag" should {
    "fail gracefully" in {
      val result = controller.ocelot("oct90001", "/maybe-we-ve-got-a-teabag", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val terminal = html.select(".terminal")
      assert(terminal.size() == 1)
    }
  }

  "GET /yes-they-do-have-a-tea-bag/yes-they-do-have-a-cup" should {
    "have the right back link" in {
      val result = controller.ocelot("oct90001", "/yes-they-do-have-a-tea-bag/yes-they-do-have-a-cup", None).apply(fakeRequest)
      val html = Jsoup.parse(contentAsString(result))

      val back = html.select(".link-back")

      assert(back.size() == 1)
      assert(back.get(0).attr("href") == "/ocelot-external-frontend/oct90001/yes-they-do-have-a-tea-bag")
    }
  }

  "GET /oct90003/test-answer" should {
    "Give a redirect" in {
      val result = controller.ocelot("oct90003", "/test-answer", None).apply(fakeRequest)

      status(result) shouldBe Status.FOUND
      header("Location", result) shouldBe Some("https://gov.uk/")

    }
  }
}