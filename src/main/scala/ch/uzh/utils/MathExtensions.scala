package ch.uzh.utils

object MathExtensions {

  /**
    * round on decimal
    * @param p decimal count
    * @param n number to round
    * @return rounded number
    */
  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}
