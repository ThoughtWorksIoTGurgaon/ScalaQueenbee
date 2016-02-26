package src.main.scala.com.supersaiyyans

import play.api.mvc.{WithFilters, EssentialAction, EssentialFilter, RequestHeader}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class CorsFilter extends EssentialFilter {
  def apply(next: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      next(requestHeader).map { result =>
        println(s"Request headers---->\n ${requestHeader.headers}\n\n\n-----------------------------------------------")
        result.withHeaders("Access-Control-Allow-Origin" -> "*"
        )
      }
    }
  }
}

object Global extends WithFilters(new CorsFilter){

}
