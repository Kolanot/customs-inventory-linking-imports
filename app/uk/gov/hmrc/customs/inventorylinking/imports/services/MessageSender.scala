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

package uk.gov.hmrc.customs.inventorylinking.imports.services

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.customs.inventorylinking.imports.connectors.{ImportsConnector, OutgoingRequestBuilder}
import uk.gov.hmrc.customs.inventorylinking.imports.model.{GoodsArrival, ImportsMessageType, RequestInfo, ValidateMovement}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class MessageSender @Inject()(outgoingRequestBuilder: OutgoingRequestBuilder,
                              goodsArrivalXmlValidationService: GoodsArrivalXmlValidationService,
                              validateMovementXmlValidationService: ValidateMovementXmlValidationService,
                              connector: ImportsConnector) {

  def send(messageType: ImportsMessageType, body: NodeSeq, requestInfo: RequestInfo, headers: Map[String, String]): Future[HttpResponse] = {
    val outgoingRequest = outgoingRequestBuilder.build(messageType, requestInfo, headers, body)

    def service = messageType match {
      case GoodsArrival => goodsArrivalXmlValidationService
      case ValidateMovement => validateMovementXmlValidationService
    }

    for {
      _ <- service.validate(body)
      result <- connector.post(outgoingRequest)
    } yield result
  }
}