package ch.uzh.sentiment

import java.io.InputStream

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf
import ch.uzh.sentiment.SentimentUtils._
import ch.uzh.utils.Smiley
import ch.uzh.utils.MathExtensions.roundAt

/**
  * Analyse Sentiments using Vader algorithm
  * Scala conversion of https://github.com/cjhutto/vaderSentiment
  */
object SentimentIntensityAnalyser {

  // check for special case idioms using a sentiment-laden keyword known to VADER
  val specialCaseIdioms = Map("the shit" -> 3.0,
                              "the bomb" -> 3.0,
                              "bad ass" -> 1.5,
                              "yeah right" -> -2.0,
                              "cut the mustard" -> 2.0,
                              "kiss of death" -> -1.5,
                              "hand to mouth" -> -2.0)

  val stream : InputStream = getClass.getResourceAsStream("/lexicon.txt")
  val lines: Iterator[String] = scala.io.Source.fromInputStream( stream )("UTF-8").getLines
  val lexicon: Map[String, Double] = lines.map(l => l.split('\t').head -> l.split('\t').tail.head.toDouble).toMap

  /**
    * Return a float for sentiment strength based on the input text.
    * Positive values are positive valence, negative value are negative valence.
    * @param text: string of text to analyse
    * @return map of negative neutral positive and compound sentiment
    *         { 'pos' -> 0.0,
    *           'neg' -> 0.0,
    *           'neu' -> 0.0,
    *           'compound' -> 0.0
    *         }
    */
  def polarityScores(text: String): Map[String, Double] = {
    val wordsAndEmoticons = SentimentText.wordsAndEmoticons(text)
    val butIndexes = wordsAndEmoticons.zipWithIndex.filter(we => we._1 == "but" || we._1 == "BUT").map(_._2)
    val sentiments = wordsAndEmoticons.view.zipWithIndex.map { wordWithIndex =>
      val word = wordWithIndex._1
      val index = wordWithIndex._2
      if ((index < wordsAndEmoticons.length - 1 && word.toLowerCase == "kind" &&
        wordsAndEmoticons(index + 1).toLowerCase == "of") || boosterDictionary.contains(word)) {
        0.0
      } else {
        if (butIndexes.isEmpty) {
          sentimentValence(wordsAndEmoticons, word, index)
        } else {
          if (index < butIndexes.head) {
            sentimentValence(wordsAndEmoticons, word, index) * 0.5
          } else {
            sentimentValence(wordsAndEmoticons, word, index) * 1.5
          }
        }
      }
    }
    scoreValence(sentiments, text)
  }


  /**
    * compute our end scores
    * @param sentiments list of all sentiments
    * @param text original text
    * @return map of negative neutral positive and compound sentiment
    */
  private def scoreValence(sentiments: Seq[Double], text: String): Map[String, Double] = {
    if (sentiments.nonEmpty) {
      var sumS = sentiments.sum
      val pe = punctuationEmphasis(text)
      if (sumS > 0.0) {
        sumS = sumS + pe
      } else if (sumS < 0.0) {
        sumS = sumS - pe
      }
      val compound = normalize(sumS)
      var posSum = sentiments.filter(_ > 0.0).map(_ + 1).sum //compensates for neutral words that are counted as 1
      var negSum = sentiments.filter(_ < 0.0).map(_ - 1).sum //when used with math.fabs(), compensates for neutrals
      val neuCount = sentiments.count(_ == 0.0)

      if (posSum > math.abs(negSum)) {
        posSum = posSum + pe
      } else if (posSum < math.abs(negSum)) {
        negSum = negSum - pe
      }
      val total = posSum + math.abs(negSum) + neuCount

      Map("pos" -> roundAt(3)(math.abs(posSum/total)),
          "neg" -> roundAt(3)(math.abs(negSum/total)),
          "neu" -> roundAt(3)(math.abs(neuCount/total)),
          "compound" -> roundAt(4)(compound))
    } else {
      Map("neg" -> 0.0, "neu" -> 0.0, "pos" -> 0.0, "compound" -> 0.0)
    }
  }

  /**
    * compute sentiment valence of the word
    * @param wordsAndEmoticons list of text
    * @param word word to analyse
    * @param index current word index
    * @return valence
    */
  private def sentimentValence(wordsAndEmoticons: Array[String], word: String, index: Int): Double = {
    val capDiff = allCapitalDifferential(wordsAndEmoticons.toList)
    if (lexicon.contains(word.toLowerCase)) {
      def determineC(word: String, capDiff: Boolean, valence: Double): Double = {
        if (Smiley.isSmiley(word)) {
          valence
        } else if ((word.toUpperCase == word) && capDiff) {
          if (valence > 0) {
            valence + cIncrease
          } else {
            valence - cIncrease
          }
        } else {
          valence
        }
      }
      var valence = lexicon(word.toLowerCase)
      valence = determineC(word, capDiff, valence)
      for (i <- 0 until 3) {
        if ((index > i) && (!lexicon.contains(wordsAndEmoticons(index - (i + 1)).toLowerCase))) {
          var scalar = determineScalar(wordsAndEmoticons(index - (i + 1)), valence, capDiff)
          if ((i == 1) && (scalar != 0)) {
            scalar = scalar * 0.95
          }
          if (( i == 2) && (scalar != 0)) {
            scalar = scalar * 0.9
          }
          valence = neverCheck(valence + scalar, wordsAndEmoticons, i, index)
          if (i == 2) {
            valence = idiomsCheck(valence, wordsAndEmoticons, index)
          }
        }
      }
      leastCheck(valence, wordsAndEmoticons, index)
    } else {
      0.0
    }
  }

  /**
    * check for negation case using "least"
    * @param valence initial starting valence
    * @param wordsAndEmoticons list of text
    * @param index current word index
    * @return adapted valence
    */
  private def leastCheck(valence: Double, wordsAndEmoticons: Array[String], index: Int): Double = {
    var localValence = valence
    if (index > 1 && !lexicon.contains(wordsAndEmoticons(index - 1).toLowerCase) && wordsAndEmoticons(index - 1).toLowerCase == "least") {
      val wordMinTwo = wordsAndEmoticons(index - 2).toLowerCase
      if (wordMinTwo != "at" && wordMinTwo != "very") {
        localValence = localValence * nScalar
      }
    } else if (index > 0 && !lexicon.contains(wordsAndEmoticons(index - 1).toLowerCase) && wordsAndEmoticons(index - 1).toLowerCase == "least") {
      localValence = localValence * nScalar
    }

    localValence
  }

  /**
    * check for booster/dampener bi-grams such as 'sort of' or 'kind of'
    * @param valence initial starting valence
    * @param wordsAndEmoticons list of text
    * @param index current word index
    * @return adapted valence
    */
  private def idiomsCheck(valence: Double, wordsAndEmoticons: Array[String], index: Int): Double = {
    var localValence = valence

    val wordOnIndex = wordsAndEmoticons(index)
    val wordMinusOne = wordsAndEmoticons(index - 1)
    val wordMinusTwo = wordsAndEmoticons(index - 2)
    val wordMinusThree = wordsAndEmoticons(index - 3)

    val oneZero = List(wordMinusOne, wordOnIndex).mkString(" ")
    val twoOneZero = List(wordMinusTwo, wordMinusOne, wordOnIndex).mkString(" ")
    val twoOne = List(wordMinusTwo, wordMinusOne).mkString(" ")
    val threeTwoOne = List(wordMinusThree, wordMinusTwo, wordMinusOne).mkString(" ")
    val threeTwo = List(wordMinusThree, wordMinusTwo).mkString(" ")
    val sequences = List(oneZero, twoOneZero, twoOne, threeTwoOne, threeTwo)

    if (specialCaseIdioms.keys.toList.intersect(sequences).nonEmpty) {
      val key = specialCaseIdioms.keys.toList.intersect(sequences).head
      localValence = specialCaseIdioms(key)
    }
    if (wordsAndEmoticons.length - 1 > index) {
      val key = List(wordOnIndex, wordsAndEmoticons(index + 1)).mkString(" ")
      if (specialCaseIdioms.keys.toList.contains(key)) {
        localValence = specialCaseIdioms(key)
      }
    }
    if (wordsAndEmoticons.length - 1 > index + 1) {
      val key = List(wordOnIndex, wordsAndEmoticons(index + 1), wordsAndEmoticons(index + 2)).mkString(" ")
      if (specialCaseIdioms.keys.toList.contains(key)) {
        localValence = specialCaseIdioms(key)
      }
    }
    if (boosterDictionary.contains(threeTwo) || boosterDictionary.contains(twoOne)) {
      localValence = localValence + bDecrease
    }

    localValence
  }

  /**
    * check for negations from 'never'
    * @param valence initial starting valence
    * @param wordsAndEmoticons list of text
    * @param wrappedIndex processor index
    * @param index current word index
    * @return adapted valence
    */
  private def neverCheck(valence: Double, wordsAndEmoticons: Array[String], wrappedIndex: Int, index: Int): Double = {
    var localValence = valence
    if ((wrappedIndex == 0) && negated(List(wordsAndEmoticons(index - 1)))) {
      localValence = localValence * nScalar
    }
    if (wrappedIndex == 1) {
      if (wordsAndEmoticons(index - 2).toLowerCase == "never" &&
        (wordsAndEmoticons(index - 1).toLowerCase == "so" || wordsAndEmoticons(index - 1).toLowerCase == "this")) {
        localValence = localValence * 1.5
      } else if (negated(List(wordsAndEmoticons(index - (wrappedIndex + 1))))) {
        localValence = localValence * nScalar
      }
    }
    if (wrappedIndex == 2) {
      if (wordsAndEmoticons(index - 3).toLowerCase == "never" &&
        (wordsAndEmoticons(index - 2).toLowerCase == "so" || wordsAndEmoticons(index - 2).toLowerCase == "this") ||
        (wordsAndEmoticons(index - 1).toLowerCase == "so" || wordsAndEmoticons(index - 1).toLowerCase == "this")) {
        localValence = localValence * 1.25
      } else if (negated(List(wordsAndEmoticons(index - (wrappedIndex + 1))))) {
        localValence = localValence * nScalar
      }
    }
    localValence
  }

  /**
    * combined punctuation emphasis for exclamation and question marks
    * @param text to analyse
    * @return
    */
  private def punctuationEmphasis(text: String): Double = {
    amplifyEm(text) + amplifyQm(text)
  }

  /**
    * check for added emphasis resulting from exclamation marks (up to 4 of them)
    * @param text to analyse
    * @return emphasis amplifier
    */
  private def amplifyEm(text: String): Double = {
    val em_count = text.count(_ == '!')
    if (em_count > 4) {
      4 * 0.292
    } else {
      em_count * 0.292
    }
  }

  /**
    * check for added emphasis resulting from question marks (2 or 3+)
    * @param text text to analyse
    * @return emphasis amplifier
    */
  private def amplifyQm(text: String): Double = {
    val qm_count = text.count(_ == '?')
    if (qm_count > 1) {
      if (qm_count <= 3) {
        qm_count * 0.18
      } else {
        0.96
      }
    } else {
      0.0
    }
  }

}

