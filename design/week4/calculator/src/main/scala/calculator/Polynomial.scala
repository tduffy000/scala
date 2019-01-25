package calculator

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] = {
    Signal( b() * b() - 4 * a() * c() )
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {
    Signal {
      var roots = Set[Double]()
      var d = computeDelta(a, b, c)()
      if ( d == 0 ) {
        roots += -b() / (2 * a())
      } else {
        roots += ( -b() + math.sqrt(d) ) / (2 * a())
        roots += ( -b() - math.sqrt(d) ) / (2 * a())
      }
      roots
    }
  }
}
