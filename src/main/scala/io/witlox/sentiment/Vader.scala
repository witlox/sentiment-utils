package io.witlox.sentiment

import SentimentIntensityAnalyser.polarityScores
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf

/**
  * VADER (Valence Aware Dictionary and sEntiment Reasoner) is a lexicon and rule-based sentiment analysis tool
  * that is specifically attuned to sentiments expressed in social media, and works well on texts from other domains.
  */
object Vader{

  /**
    * spark helper UDF for analysing positive polarity between 0.0 and 1.0
    * @return positivity polarity
    */
  def positive: UserDefinedFunction = udf((t: String) => polarityScores(t)("pos"))

  /**
    * spark helper UDF for analysing negative polarity between 0.0 and 1.0
    * @return negative polarity
    */
  def negative: UserDefinedFunction = udf((t: String) => polarityScores(t)("neg"))

  /**
    * spark helper UDF for analysing neutral polarity between 0.0 and 1.0
    * @return neutral polarity
    */
  def neutral: UserDefinedFunction = udf((t: String) => polarityScores(t)("neu"))

  /**
    * spark helper UDF for analysing compound polarity
    * positive sentiment: compound score >= 0.5
    * neutral sentiment: (compound score > -0.5) and (compound score < 0.5)
    * negative sentiment: compound score <= -0.5
    * @return compound polarity
    */
  def compound: UserDefinedFunction = udf((t: String) => polarityScores(t)("compound"))
}
