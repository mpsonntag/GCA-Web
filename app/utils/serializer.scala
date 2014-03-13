package utils

import java.net.URL
import collection.JavaConversions._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._

package object serializer {

  implicit val urlWrites = new Writes[URL] {
    def writes(url: URL) = {
      if (url == null) {
        JsNull
      } else {
        Json.toJson(url.toString)
      }
    }
  }

  /**
   * Conference serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class ConferenceFormat(implicit routesResolver: RoutesResolver) extends Format[Conference] {

    override def reads(json: JsValue): JsResult[Conference] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "name").readNullable[String]
    )(Conference(_, _)).reads(json)

    override def writes(c: Conference): JsValue = {
      Json.obj(
        "name" -> c.name,
        "uuid" -> c.uuid,
        "abstracts" -> routesResolver.abstractsUrl(c.uuid)
      )
    }
  }

  /**
   * Account serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class AccountFormat(implicit routesResolver: RoutesResolver) extends Format[Account] {

    override def reads(json: JsValue): JsResult[Account] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "mail").readNullable[String]
    )(Account(_, _)).reads(json)

    override def writes(account: Account): JsValue = {
      Json.obj(
        "uuid" -> account.uuid,
        "email" -> account.mail,
        "abstracts" -> routesResolver.abstractsUrl(account.uuid)
      )
    }
  }

  /**
   * Author serializer.
   */
  class AuthorFormat extends Format[Author] {

    override def reads(json: JsValue): JsResult[Author] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "mail").readNullable[String] and
      (__ \ "firstName").readNullable[String] and
      (__ \ "middleName").readNullable[String] and
      (__ \ "lastName").readNullable[String]
    )(Author(_, _, _, _, _)).reads(json)

    override def writes(a: Author): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "email" -> a.mail,
        "firstName" -> a.firstName,
        "middleName" -> a.middleName,
        "lastName" -> a.lastName
      )
    }
  }

  /**
   * Affiliation serializer.
   */
  class AffiliationFormat extends Format[Affiliation] {

    override def reads(json: JsValue): JsResult[Affiliation] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "address").readNullable[String] and
      (__ \ "country").readNullable[String] and
      (__ \ "department").readNullable[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "section").readNullable[String]
    )(Affiliation(_, _, _, _, _, _)).reads(json)

    override def writes(a: Affiliation): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "address" -> a.address,
        "country" -> a.country,
        "department" -> a.department,
        "name" -> a.name,
        "section" -> a.section
      )
    }
  }

  /**
   * Reference serializer.
   */
  class ReferenceFormat extends Format[Reference] {

    override def reads(json: JsValue): JsResult[Reference] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "authors").readNullable[String] and
      (__ \ "title").readNullable[String] and
      (__ \ "year").readNullable[Int] and
      (__ \ "doi").readNullable[String]
    )(Reference(_, _, _, _, _)).reads(json)

    override def writes(a: Reference): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "authors" -> a.authors,
        "title" -> a.title,
        "year" -> a.year,
        "doi" -> a.doi
      )
    }
  }

  /**
   * Figure serializer.
   */
  class FigureFormat extends Format[Figure] {

    override def reads(json: JsValue): JsResult[Figure] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "caption").readNullable[String]
    )(Figure(_, _, _)).reads(json)

    override def writes(a: Figure): JsValue = {
      if (a == null) {
        JsNull
      } else {
        Json.obj(
          "uuid" -> a.uuid,
          "name" -> a.name,
          "caption" -> a.caption,
          "URL" -> a.uuid  // TODO: build URL
        )
      }
    }
  }

  /**
   * Abstract serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class AbstractFormat(implicit routesResolver: RoutesResolver) extends Format[Abstract] with ConstraintReads {

    val authorF = new AuthorFormat()
    val affiliationF = new AffiliationFormat()
    val referenceF = new ReferenceFormat()
    implicit val figureF = new FigureFormat()


    override def reads(json: JsValue): JsResult[Abstract] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "title").readNullable[String] and
      (__ \ "topic").readNullable[String] and
      (__ \ "text").readNullable[String] and
      (__ \ "doi").readNullable[String] and
      (__ \ "conflictOfInterest").readNullable[String] and
      (__ \ "acknowledgements").readNullable[String] and
      (__ \ "approved").read[Boolean] and
      (__ \ "published").read[Boolean] and
      (__ \ "authors").lazyRead( list[Author](authorF) ) and
      (__ \ "affiliations").lazyRead( list[Affiliation](affiliationF) ) and
      (__ \ "references").lazyRead( list[Reference](referenceF) )
    )(Abstract(_, _, _, _, _, _, _, _, _, None, None, Nil, _, _, _)).reads(json)

    override def writes(a: Abstract): JsValue = {
      val authors: Seq[Author] = asScalaSet(a.authors).toSeq
      val affiliations: Seq[Affiliation] = asScalaSet(a.affiliations).toSeq
      val references: Seq[Reference] = asScalaSet(a.references).toSeq
      Json.obj(
        "uuid" -> a.uuid,
        "title" -> a.title,
        "topic" -> a.topic,
        "text" -> a.text,
        "doi" -> a.doi,
        "conflictOfInterest" -> a.conflictOfInterest,
        "acknowledgements" -> a.acknowledgements,
        "approved" -> a.approved,
        "published" -> a.published,
        "conference" -> a.conference.uuid,
        "figure" -> a.figure,
        "owners" -> routesResolver.ownersUrl(a.uuid),
        "authors" -> JsArray( for (auth <- authors) yield authorF.writes(auth) ),
        "affiliations" -> JsArray( for (auth <- affiliations) yield affiliationF.writes(auth) ),
        "references" -> JsArray( for (auth <- references) yield referenceF.writes(auth) )
      )
    }
  }

}