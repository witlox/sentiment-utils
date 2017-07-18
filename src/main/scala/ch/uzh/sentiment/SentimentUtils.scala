package ch.uzh.sentiment

/**
  * Scala conversion of https://github.com/cjhutto/vaderSentiment
  */
object SentimentUtils {

  // (empirically derived mean sentiment intensity rating increase for booster words)
  val bIncrease: Double = 0.293
  val bDecrease: Double = -0.293

  // (empirically derived mean sentiment intensity rating increase for using ALLCAPs to emphasize a word)
  val cIncrease: Double = 0.733
  val nScalar: Double = -0.74

  val negate = Seq("aint", "arent", "cannot", "cant", "couldnt", "darent", "didnt", "doesnt", "ain't", "aren't", "can't",
    "couldn't", "daren't", "didn't", "doesn't", "dont", "hadnt", "hasnt", "havent", "isnt", "mightnt", "mustnt", "neither",
    "don't", "hadn't", "hasn't", "haven't", "isn't", "mightn't", "mustn't", "neednt", "needn't", "never", "none", "nope",
    "nor", "not", "nothing", "nowhere", "oughtnt", "shant", "shouldnt", "uhuh", "wasnt", "werent", "oughtn't", "shan't",
    "shouldn't", "uh-uh", "wasn't", "weren't", "without", "wont", "wouldnt", "won't", "wouldn't", "rarely", "seldom",
    "despite")

  // booster/dampener 'intensifiers' or 'degree adverbs' http://en.wiktionary.org/wiki/Category:English_degree_adverbs
  val boosterDictionary = Map("absolutely" -> bIncrease, "amazingly"-> bIncrease, "awfully"-> bIncrease, "completely"-> bIncrease,
    "considerably"-> bIncrease, "decidedly"-> bIncrease, "deeply"-> bIncrease, "effing"-> bIncrease, "enormously"-> bIncrease,
    "entirely"-> bIncrease, "especially"-> bIncrease, "exceptionally"-> bIncrease, "extremely"-> bIncrease, "fabulously"-> bIncrease,
    "flipping"-> bIncrease, "flippin"-> bIncrease, "fricking"-> bIncrease, "frickin"-> bIncrease, "frigging"-> bIncrease,
    "friggin"-> bIncrease, "fully"-> bIncrease, "fucking"-> bIncrease, "greatly"-> bIncrease, "hella"-> bIncrease, "highly"-> bIncrease,
    "hugely"-> bIncrease, "incredibly"-> bIncrease, "intensely"-> bIncrease, "majorly"-> bIncrease, "more"-> bIncrease, "most"-> bIncrease,
    "particularly"-> bIncrease, "purely"-> bIncrease, "quite"-> bIncrease, "really"-> bIncrease, "remarkably"-> bIncrease, "so"-> bIncrease,
    "substantially"-> bIncrease, "thoroughly"-> bIncrease, "totally"-> bIncrease, "tremendously"-> bIncrease, "uber"-> bIncrease,
    "unbelievably"-> bIncrease, "unusually"-> bIncrease, "utterly"-> bIncrease, "very"-> bIncrease, "almost"-> bDecrease,
    "barely"-> bDecrease, "hardly"-> bDecrease, "just enough"-> bDecrease, "kind of"-> bDecrease, "kinda"-> bDecrease, "kindof"-> bDecrease,
    "kind-of"-> bDecrease,    "less"-> bDecrease, "little"-> bDecrease, "marginally"-> bDecrease, "occasionally"-> bDecrease, "partly"-> bDecrease,
    "scarcely"-> bDecrease, "slightly"-> bDecrease, "somewhat"-> bDecrease, "sort of"-> bDecrease, "sorta"-> bDecrease, "sortof"-> bDecrease,
    "sort-of"-> bDecrease)


  /**
    * Determine if input contains negation words
    * @param words list of words
    * @param include_nt include n't
    * @return negated or not
    */
  def negated(words: List[String], include_nt: Boolean = true): Boolean = {
    if (words.isEmpty) {
      false
    } else if (words.intersect(negate).nonEmpty) {
      true
    } else if (include_nt && words.exists(w => "n't".r.findAllIn(w).nonEmpty)) {
      true
    } else if (words.length > 2 && words.sliding(2).exists(w => w.head.equals("at") && w.tail.head.equals("least"))) {
      true
    } else {
      false
    }
  }

  /**
    * Normalize the score to be between -1 and 1 using an alpha that
    * approximates the max expected value
    * @param score original
    * @param alpha approximator
    * @return normalized score
    */
  def normalize(score: Double, alpha: Int = 15): Double = {
    val norm_score = score / math.sqrt((score * score) + alpha)
    if (norm_score < -1.0) {
      -1.0
    } else if (norm_score > 1.0) {
      1.0
    } else {
      norm_score
    }
  }

  /**
    * Check whether just some words in the input are ALL CAPS
    * @param words list of words
    * @return all capitals yes or no
    */
  def allCapitalDifferential(words: List[String]): Boolean = {
    val cap_len = words.count(w => w.toUpperCase == w)
    cap_len > 0 && cap_len < words.length
  }

  /**
    * Check if the preceding words increase, decrease, or negate/nullify the valence
    * @param word the word to analyse
    * @param valence original valence
    * @param isCapDiff capital differential
    * @return scalar
    */
  def determineScalar(word: String, valence: Double, isCapDiff: Boolean): Double = {
    if (boosterDictionary.contains(word.toLowerCase())) {
      var scalar = boosterDictionary(word.toLowerCase())
      if (valence < 0) {
        scalar *= -1
      }
      if (word.toUpperCase() == word && isCapDiff) {
        if (valence > 0) {
          scalar += cIncrease
        } else {
          scalar -= cIncrease
        }
      }
      scalar
    } else {
      0.0
    }
  }

}
