package com.supersaiyyans.util

object Logger {
  def debugWithArgs(message: String, args: (String,String)*) = {
    debug(message)
    args.foreach(x=>debug(s"${x._1}: ${x._2}"))
  }
  def debug(message: String) = {
    play.api.Logger.debug(s"\n-------------$message------------")
  }

}
