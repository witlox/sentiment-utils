package ch.uzh.sentiment

import org.scalatest.{FlatSpec, Matchers}
import ch.uzh.sentiment.SentimentText.{wordsPlusPunctuation, wordsAndEmoticons}

class SentimentTests extends FlatSpec with Matchers {

  "SentimentText wordsPlusPunctuation" should "generate all possible punctuations" in {
    val wpp = wordsPlusPunctuation("cat")
    wpp.size should be (34)
    wpp.head._1 should be ("cat;")
    wpp.head._2 should be ("cat")
  }

  "SentimentText wordsAndEmoticons" should "should remove leading and trailing punctuations" in {
    wordsAndEmoticons(".the cat, with :) the hat.") should be (Array("the", "cat", "with", ":)", "the", "hat"))
  }

  "SentimentIntensityAnalyser" should "work for simple sentences" in {
    SentimentIntensityAnalyser.polarityScores("VADER is smart, handsome, and funny.") should be (Map("pos"-> 0.746, "neg"-> 0.0, "neu"-> 0.254, "compound"-> 0.8316))
    SentimentIntensityAnalyser.polarityScores("VADER is not smart, handsome, nor funny.") should be (Map("pos"-> 0.0, "neg"-> 0.646, "neu"-> 0.354, "compound"-> -0.7424))
    SentimentIntensityAnalyser.polarityScores("VADER is smart, handsome, and funny!") should be (Map("pos"-> 0.752, "neg"-> 0.0, "neu"-> 0.248, "compound"-> 0.8439))
    SentimentIntensityAnalyser.polarityScores("VADER is very smart, handsome, and funny.") should be (Map("pos"-> 0.701, "neg"-> 0.0, "neu"-> 0.299, "compound"-> 0.8545))
    SentimentIntensityAnalyser.polarityScores("VADER is VERY SMART, handsome, and FUNNY.") should be (Map("pos"-> 0.754, "neg"-> 0.0, "neu"-> 0.246, "compound"-> 0.9227))
    SentimentIntensityAnalyser.polarityScores("VADER is VERY SMART, handsome, and FUNNY!!!") should be (Map("pos"-> 0.767, "neg"-> 0.0, "neu"-> 0.233, "compound"-> 0.9342))
    SentimentIntensityAnalyser.polarityScores("VADER is VERY SMART, uber handsome, and FRIGGIN FUNNY!!!") should be (Map("pos"-> 0.706, "neg"-> 0.0, "neu"-> 0.294, "compound"-> 0.9469))
    SentimentIntensityAnalyser.polarityScores("The book was good.") should be (Map("pos"-> 0.492, "neg"-> 0.0, "neu"-> 0.508, "compound"-> 0.4404))
    SentimentIntensityAnalyser.polarityScores("The book was kind of good.") should be (Map("pos"-> 0.343, "neg"-> 0.0, "neu"-> 0.657, "compound"-> 0.3832))
    SentimentIntensityAnalyser.polarityScores("The plot was good, but the characters are uncompelling and the dialog is not great.") should be (Map("pos"-> 0.094, "neg"-> 0.327, "neu"-> 0.579, "compound"-> -0.7042))
    SentimentIntensityAnalyser.polarityScores("At least it isn't a horrible book.") should be (Map("pos"-> 0.363, "neg"-> 0.0, "neu"-> 0.637, "compound"-> 0.431))
    SentimentIntensityAnalyser.polarityScores("Make sure you :) or :D today!") should be (Map("pos"-> 0.69, "neg"-> 0.0, "neu"-> 0.31, "compound"-> 0.8356))
    SentimentIntensityAnalyser.polarityScores("Today SUX!") should be (Map("pos"-> 0.0, "neg"-> 0.779, "neu"-> 0.221, "compound"-> -0.5461))
    SentimentIntensityAnalyser.polarityScores("Today only kinda sux! But I'll get by, lol") should be (Map("pos"-> 0.251, "neg"-> 0.179, "neu"-> 0.569, "compound"-> 0.2228))
  }
  
  "SentimentIntensityAnalyser" should "work for more tricky sentences" in {
    SentimentIntensityAnalyser.polarityScores("Sentiment analysis has never been good.") should be  (Map("pos"-> 0.0, "neg"-> 0.325, "neu"-> 0.675, "compound"-> -0.3412))
    SentimentIntensityAnalyser.polarityScores("Sentiment analysis has never been this good!") should be  (Map("pos"-> 0.379, "neg"-> 0.0, "neu"-> 0.621, "compound"-> 0.5672))
    SentimentIntensityAnalyser.polarityScores("Most automated sentiment analysis tools are shit.") should be  (Map("pos"-> 0.0, "neg"-> 0.375, "neu"-> 0.625, "compound"-> -0.5574))
    SentimentIntensityAnalyser.polarityScores("With VADER, sentiment analysis is the shit!") should be  (Map("pos"-> 0.417, "neg"-> 0.0, "neu"-> 0.583, "compound"-> 0.6476))
    SentimentIntensityAnalyser.polarityScores("Other sentiment analysis tools can be quite bad.") should be  (Map("pos"-> 0.0, "neg"-> 0.351, "neu"-> 0.649, "compound"-> -0.5849))
    SentimentIntensityAnalyser.polarityScores("On the other hand, VADER is quite bad ass!") should be  (Map("pos"-> 0.586, "neg"-> 0.0, "neu"-> 0.414, "compound"-> 0.8172))
    SentimentIntensityAnalyser.polarityScores("Roger Dodger is one of the most compelling variations on this theme.") should be  (Map("pos"-> 0.166, "neg"-> 0.0, "neu"-> 0.834, "compound"-> 0.2944))
    SentimentIntensityAnalyser.polarityScores("Roger Dodger is one of the least compelling variations on this theme.") should be  (Map("pos"-> 0.0, "neg"-> 0.132, "neu"-> 0.868, "compound"-> -0.1695))
    SentimentIntensityAnalyser.polarityScores("Roger Dodger is at least compelling as a variation on the theme.") should be (Map("pos"-> 0.16, "neg"-> 0.0, "neu"-> 0.84, "compound"-> 0.2263))
  }
}
