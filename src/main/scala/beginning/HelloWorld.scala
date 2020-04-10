package beginning

import zio._
import zio.console._
//object HelloWorld extends App {
//  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
//    // putStrLn("Hello World").map(_ => 0)
//    //    putStrLn("Hello").flatMap(_ => putStrLn("World")).as(0)
//
//    //putStrLn("Hello") *> putStrLn("World") *> ZIO.succeed(0)
//
//    //    val failed =
//    ////      putStrLn("About to Fail") *>
//    ////    ZIO.fail("Failure") *>
//    ////    putStrLn("Should not be printed ")
//    ////
//    ////
//    ////    //(failed as 0).catchAllCause(cause => putStrLn(s"${cause.prettyPrint}") as 1)
//    ////
//    ////
//    ////    def repeat[R,E,A](n : Int)(effect : ZIO[R,E,A]) : ZIO[R,E,A] = {
//    ////      if( n <= 1) effect
//    ////      else effect *> repeat(n - 1)(effect)
//    ////    }
//    ////
//    ////
//    ////    repeat(1000000000)(putStrLn("Hello World") as 0)
//    ////
//    ////  }
//
//    putStr("Hello").flatMap( _ => ZIO.succeed(0))
//    putStr("Hello") as 0
//
//  }
//}
