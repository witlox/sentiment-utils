package ch.uzh.sentiment

import ch.uzh.utils.{Emoji, Smiley, TimePartition}
import org.scalatest.{FlatSpec, Matchers}

class UtilsTests extends FlatSpec with Matchers {

  "Emoji detection" should "actually detect emojis" in {
    Emoji.isEmoji("⚓") should be (true)
    Emoji.getEmojiString("⚓") should be ("anchor")
  }

  "Smiley detection" should "actually detect smileys" in {
    Smiley.isSmiley(":)") should be (true)
    Smiley.isPositive(":)") should be (true)
    Smiley.isNegative(":(") should be (true)
  }

  "Bucketizing a time value" should "partition correctly by day" in {
    val tp = TimePartition("day")
    tp.bucketize("2012-01-01T01:01:01.000Z") should be ("2012-01-01")
    tp.bucketize("2012-01-01T23:59:59.000Z") should be ("2012-01-01")
    tp.bucketize("2014-03-17T11:01:03.000Z") should be ("2014-03-17")
  }

  "Bucketizing a time value" should "partition correctly by year" in {
    val tp = TimePartition("year")
    tp.bucketize("2012-01-01T01:01:01.000Z") should be ("2012")
    tp.bucketize("2012-11-21T23:59:59.000Z") should be ("2012")
    tp.bucketize("2014-03-17T11:01:03.000Z") should be ("2014")
  }
}
