package ch.uzh.sentiment

import ch.uzh.utils.Emoji

import scala.util.matching.Regex

/**
  * Scala conversion of https://github.com/cjhutto/vaderSentiment
  */
object SentimentText {

  private val punctuations = Seq(".", "!", "?", ",", ";", ":", "-", "'", "\"", "!!", "!!!", "??", "???", "?!?", "!?!", "?!?!", "!?!?")
  private val removePunctuations: Regex = """[\p{Punct}]|\b\p{IsLetter}{1}\b\s*""".r

  /**
    * Map the words in a text to all possible punctuations as a map
    * Key is punctuated word, value is original word
    * ex. {
    *       'cat.' , 'cat'
    *       '.cat' , 'cat'
    *     }
    * @param text original text
    * @return all possible puntuations
    */
  def wordsPlusPunctuation(text: String): Map[String, String] = {
    val textWithoutPunctuation = removePunctuations.replaceAllIn(text, "")
    (for {
      word <- textWithoutPunctuation.split(' ')
      filteredWord <- Emoji.getEmojiString(word).split(' ')
      punctuation <- punctuations
    } yield List((punctuation + filteredWord, filteredWord), (filteredWord + punctuation, filteredWord)).toMap) reduce (_ ++ _)
  }

  /**
    * Removes leading and trailing punctuation
    * Leaves contractions and most emoticons
    * Does not preserve punc-plus-letter emoticons (e.g. :D)
    * @param text original text as string
    * @return Array of words
    */
  def wordsAndEmoticons(text: String): Array[String] = {
    val wpp = wordsPlusPunctuation(text)
    def resultOr(w: String) = if (wpp.contains(w)) wpp(w) else w
    for {
      word <- text.split(' ')
      if word.length > 1
    } yield resultOr(word)
  }
}
