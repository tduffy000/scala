package recfun

object Main {

  def main(args: Array[String]) {

    /* First Exercise */
    println("Exercise One: Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }

    /* Second Exercise */
    println()
    println("Exercise Two: Parentheses Checker")
    val balTestOne = "(if (zero? x) max (/ 1 x))"
    val balTestTwo = "I told him (that it’s not (yet) done). (But he wasn’t listening)"
    val balTestThree = ":-)"
    val balTestFour = "())("
    println("is balanced: " + balTestOne + " ? " + balance(balTestOne.toList))
    println("is balanced: " + balTestTwo + " ? " + balance(balTestTwo.toList))
    println("is balanced: " + balTestThree + " ? " + balance(balTestThree.toList))
    println("is balanced: " + balTestFour + " ? " + balance(balTestFour.toList))

    /* Third Exercise */
    println()
    println("Exercise Three: Coin Change Checker")
    val denomOne = List(1,2,3)
    val denomTwo = List(1)
    val denomThree = List(1,5,10,25)
    println("Number of change permutations for 5 cents (denomOne): " + countChange(5, denomOne))
    println("Number of change permutations for 5 cents (denomTwo): " + countChange(5, denomTwo))
    println("Number of change permutations for 5 cents (denomThree): " + countChange(5, denomThree))
  }

  /**
   * Exercise 1
   * Recursive Pascal's triangle.
   */
    def pascal(c: Int, r: Int): Int =
      // base case (the 1's)
      if(c == 0 || c == r) 1
      else pascal(c-1, r-1) + pascal(c, r-1)

  /**
   * Exercise 2
   * Recursively verify the balancing of parentheses in an input string.
   */
    def balance(chars: List[Char]): Boolean = {

      def areParensBalanced(chars : List[Char], numOpens : Int): Boolean = {
        // base case: empty string, return whether have remaining opens
        if(chars.isEmpty) numOpens == 0

        else if(chars.head == '(')
          areParensBalanced(chars.tail, numOpens + 1)

        else if(chars.head == ')')
          numOpens > 0 && areParensBalanced(chars.tail, numOpens - 1)

        else areParensBalanced(chars.tail, numOpens)
      }
      areParensBalanced(chars, 0)
    }

  /**
   * Exercise 3
   * Recursively generate the number of ways we can make change from an input
   * list of available denominations and an amount, money.
   */
    def countChange(money: Int, coins: List[Int]): Int = {
      val coinsLength = coins.size

      def getCounts(money: Int, coins: List[Int], idx: Int): Int = {
        // more than exhausted our available $; don't add case
        if( money < 0 ) 0
        // have perfectly made change; add case to overall count
        else if( money == 0 ) 1
        // have exhausted our coin options and haven't made change; don't add case
        else if( idx == coinsLength && money > 0 ) 0
        else getCounts(money - coins(idx), coins, idx) + getCounts(money, coins, idx + 1)
      }
      getCounts(money, coins, 0)
    }
}
