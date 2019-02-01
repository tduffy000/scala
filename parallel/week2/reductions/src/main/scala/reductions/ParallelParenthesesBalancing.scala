package reductions

import scala.annotation._
import org.scalameter._
import common._

object ParallelParenthesesBalancingRunner {

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime ms")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime ms")
    println(s"speedup: ${seqtime / fjtime}")
  }
}

object ParallelParenthesesBalancing {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
   def balance(chars: Array[Char]): Boolean = {

     def areParensBalanced(chars : Array[Char], nOpen : Int): Boolean = {
       if(chars.isEmpty) nOpen == 0
       else if(chars.head == '(')
         areParensBalanced(chars.tail, nOpen + 1)
       else if(chars.head == ')')
         nOpen > 0 && areParensBalanced(chars.tail, nOpen - 1)
       else areParensBalanced(chars.tail, nOpen)
     }
     areParensBalanced(chars, 0)
   }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(idx: Int, until: Int, nOpen: Int, nClosed: Int): (Int, Int) = {
      if ( idx < until ) {
        chars(idx) match {
          case '(' => traverse( idx + 1, until, nOpen + 1, nClosed)
          case ')' => {
            if ( nOpen > 0 ) traverse(idx + 1, until, nOpen - 1, nClosed)
            else traverse(idx + 1, until, nOpen, nClosed + 1)
          }
          case _ => traverse(idx + 1, until, nOpen, nClosed)
        }
      } else (nOpen, nClosed)
    }

    def reduce(from: Int, until: Int): (Int, Int) = {
      if (until - from <= threshold ) {
        val mid = from + (until - from) / 2
        val ((a,b), (c,d)) = parallel( reduce(from, mid), reduce(mid, until) )
        if( a > d ) {
          (a - d + c) -> b
        } else {
          c -> (d - a + b)
        }
      } else traverse(from, until, 0, 0)
    }
    reduce(0, chars.length) == (0,0)
  }
  // For those who want more:
  // Prove that your reduction operator is associative!

}
