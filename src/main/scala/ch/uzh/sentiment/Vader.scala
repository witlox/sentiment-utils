package ch.uzh.sentiment

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf

class Vader {

  private val sentimentIntensityAnalyser = new SentimentIntensityAnalyser

  private def pos(text: String): Double = {
    val scores = sentimentIntensityAnalyser.polarityScores(text)
    scores("pos")
  }

  def positive: UserDefinedFunction = udf(pos _)

  private def neg(text: String): Double = {
    val scores = sentimentIntensityAnalyser.polarityScores(text)
    scores("neg")
  }

  def negative: UserDefinedFunction = udf(neg _)

  private def neu(text: String): Double = {
    val scores = sentimentIntensityAnalyser.polarityScores(text)
    scores("neu")
  }

  def neutral: UserDefinedFunction = udf(neu _)

  private def comp(text: String): Double = {
    val scores = sentimentIntensityAnalyser.polarityScores(text)
    scores("compound")
  }

  def compound: UserDefinedFunction = udf(comp _)
}
