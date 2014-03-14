package controller

import scala.Some
import scala.io.Source.fromFile

import java.io.File
import org.junit._
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.api.test._
import play.api.Play
import play.api.test.Helpers._
import play.api.http.Writeable
import play.api.test.FakeApplication
import play.api.libs.json.JsObject
import play.api.mvc.Cookie

import utils.serializer.FigureFormat

/**
 * Test
 */
class FigureCtrlTest extends BaseCtrlTest {

  val formatter = new FigureFormat()
  var cookie : Cookie = _

  @Before
  override def before() : Unit = {
    super.before()
    cookie = getCookie(assets.alice.identityId, "testtest")
  }

  implicit def writeableOf_AnyContentAsMultipartFormData(implicit codec: Codec):
    Writeable[AnyContentAsMultipartFormData] = {
      val boundary = "----Af456BGe4h"
      val charset = codec.charset

      Writeable(dataParts => {

        val text: Iterable[Array[Byte]] = for (dp <- dataParts.mdf.dataParts) yield {
          val header = ("Content-Disposition: form-data; name=\"" + dp._1 + "\"\r\n\r\n").getBytes(charset)
          val data = dp._2.mkString.getBytes(charset)
          val footer = "\r\n".getBytes(charset)

          header ++ data ++ footer
        }

        val data: Iterable[Array[Byte]] = for (file <- dataParts.mdf.files) yield {
          val header = ("Content-Disposition: form-data; name=\"" + file.key +
            "\"; filename=\"" + file.filename + "\"\r\n" + "Content-Type: " +
            file.contentType.getOrElse("image/jpeg") + "\r\n\r\n").getBytes(charset)
          val data = fromFile(file.ref.file).map(_.toByte).toArray
          val footer = "\r\n".getBytes(charset)

          header ++ data ++ footer
        }

        val separator = ("--" + boundary + "\r\n").getBytes(charset)
        val body = (text ++ data).foldLeft(Array[Byte]())((r,c) => r ++ separator ++ c)

        //val debug = new String(body.map(_.toChar))
        body ++ separator ++ "--".getBytes(charset)

      }, Some(s"multipart/form-data; boundary=$boundary"))
  }

  @Test
  def testUpload(): Unit = {
    val figure = formatter.writes(assets.figures(0)).as[JsObject] - "uuid" - "URL"

    val file = new File("tmp")
    file.createNewFile()
    new java.io.FileOutputStream(file).write("foobar".getBytes)

    val requestBody = MultipartFormData(
      Map("figure" -> Seq(figure.toString())),
      Seq(MultipartFormData.FilePart(
        "file", "foo.bar",
        Some("image/jpeg"),
        TemporaryFile(file))
      ),
      Seq(),
      Seq()
    )

    val uuid = assets.abstracts(0).uuid
    val request = FakeRequest(
      POST, s"/api/abstracts/$uuid/figures"
    ).withMultipartFormDataBody(requestBody).withCookies(cookie)

    val result = route(FigureCtrlTest.app, request).get
    assert(status(result) == CREATED)
  }

  def testGet(): Unit = {
    var uuid = assets.abstracts(0).uuid // abstract that has figure
    var request = FakeRequest(GET, s"/api/abstracts/$uuid/figure")
    var result = route(FigureCtrlTest.app, request).get

    assert(status(result) == OK)
    assert(contentType(result) == Some("application/json"))
    assert(formatter.reads(contentAsJson(result)).get.uuid == uuid)

    uuid = assets.abstracts(1).uuid // no figure for this abstract
    request = FakeRequest(GET, s"/api/abstracts/$uuid/figure")
    result = route(FigureCtrlTest.app, request).get
    assert(status(result) == NOT_FOUND)
  }

  def testGetFile(): Unit = {
    val uuid = assets.abstracts(0).uuid
    val request = FakeRequest(GET, s"/abstracts/$uuid/figure/image")
    val result = route(FigureCtrlTest.app, request).get

    assert(status(result) == OK)
    val file = contentAsBytes(result)
    // TODO here make some file assert
  }
}

object FigureCtrlTest {

  var app: FakeApplication = null

  @BeforeClass
  def beforeClass() = {
    app = new FakeApplication()
    Play.start(app)
  }

  @AfterClass
  def afterClass() = {
    Play.stop()
  }

}