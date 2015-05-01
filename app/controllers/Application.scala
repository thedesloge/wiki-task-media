package controllers

import java.io.File

import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.cache.Cache
import scala.collection.mutable
import scala.concurrent.Future
import scala.io.Source
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  val articleName: String = "Latest_plane_crash"

  def index = Action {
    Cache.get(articleName) match {
      case List(article, filename) => Ok(views.html.article(article.toString()))
      case _ =>
        val article = Source.fromFile("./articles/Latest_plane_crash.html").mkString
        Ok(views.html.article(article))
    }
  }

  def getArticle = Action{
    val fibSeq = fib(34, 0, 1)

    Cache.get(articleName) match {
      case List(article, filename) => Ok(article.toString()).as(HTML)
      case _ => Ok.sendFile(
        content = new File("./articles/Latest_plane_crash.html"),
        inline = true
      )
    }
  }

  def getLatestArticleFile = Action{
    val fibSeq = fib(34, 0, 1)

    Cache.get(articleName) match {
      case List(article, filename) => Ok.sendFile(
        content = new File(filename.toString)
      ).as(HTML)

      case _ => Ok.sendFile(
        content = new File("./articles/Latest_plane_crash.html")
      )
    }


  }

  def updateForm = Action{
    Ok(views.html.updateForm())
  }

  def update = Action.async(parse.multipartFormData){ request =>
    val writeFuture = futureWriter(request.body.file(articleName))

    writeFuture.map{
      result => { if(result != "Error") Ok(views.html.updateSuccess()) else Ok(views.html.updateFailed())}
    }.recover {
      case thrown => Ok(views.html.updateFailed())
    }

  }

  def futureWriter(file: Option[MultipartFormData.FilePart[TemporaryFile]]): Future[String] = Future{
    file.map{ article =>
      val filename = "./articles/"+ articleName + "_" + System.currentTimeMillis() + ".html"
      article.ref.moveTo(new File(filename))

      val articleContents:String = Source.fromFile(filename).mkString
      Cache.set(articleName, List(articleContents, filename))
      s"File uploaded successfully"
    }.getOrElse{
      s"Error"
    }
  }

  def fib(n: Int, a: Int, b: Int): Int ={
    if(n == 0){
     b
    } else {
      fib(n -1, b , a + b)
    }
  }
}