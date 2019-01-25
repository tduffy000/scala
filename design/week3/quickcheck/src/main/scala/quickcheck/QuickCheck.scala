package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] = for {
    x <- arbitrary[Int]
    h <- oneOf(genHeap, const(empty))
  } yield insert(x, h)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  /* If we remove & re-insert the min does it get returned when we do findMin again? */
  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  /* Does the only element inserted in an empty heap get returned as the min? */
  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  /* If you insert any two elements into an empty heap, finding the minimum of
  the resulting heap should get the smallest of the two elements back. */
  property("insert 2 elements to empty, min is returned") = forAll { (a: Int, b: Int) =>
    val aIsMin = a <= b
    val h = insert(a, empty)
    val hTwo = insert(b, h)
    if(aIsMin) findMin(hTwo) == a
    else findMin(hTwo) == b
  }

 /* If you insert an element into an empty heap, then delete the minimum,
 the resulting heap should be empty.*/
 property("delMin on 1-element yields empty") = forAll{ a: Int =>
   val h = insert(a, empty)
   val hTwo = deleteMin(h)
   isEmpty(h) == true
 }

 def minAcc(h: H): List[Int] = h match {
   case h if isEmpty(h) => Nil
   case h => findMin(h) :: minAcc(deleteMin(h))
 }

 /* Given any heap, you should get a sorted sequence of elements when continually
 finding and deleting minima. */
 property("repeated delMin yields sorted sequence") = forAll{ h: H =>
   val l = minAcc(h)
   (l, l.tail).zipped.forall(_ <= _)
 }

 /* Given two heaps, you should get a sorted sequence after deleteMin repeatedly */
 property("repeated delMin on melded yields sorted sequence") = forAll{ (h1: H, h2: H) =>
   val merged = meld(h1, h2)
   val l = minAcc(merged)
   (l, l.tail).zipped.forall(_ <= _)
 }

 /* Finding a minimum of the melding of any two heaps should return a
 minimum of one or the other. */
 property("melding yields min of two prior to melding") = forAll{ (h1 : H, h2: H) =>
   val a = findMin(h1)
   val b = findMin(h2)
   if( a <= b ) findMin(meld(h1, h2)) == a
   else findMin(meld(h1, h2)) == b
 }

}
