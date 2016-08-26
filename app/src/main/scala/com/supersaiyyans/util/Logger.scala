package com.supersaiyyans.util

object Logger {
  def debugWithArgs(message: String, args: (String,String)*) = {
    println(s"\n-------------${message}------------")
    args.foreach(x=>println(s"${x._1}: ${x._2}"))
  }
  def debug(message: String) = {
    println(s"\n-------------${message}------------")
  }

}
