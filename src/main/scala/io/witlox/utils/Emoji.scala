package io.witlox.utils

import java.io.InputStream

case class EmojiData(text: String, native: String, bytes: String, rEncoding: String)

object Emoji {

  private val stream : InputStream = getClass.getResourceAsStream("/emojis.csv")
  private val lines: Iterator[String] = scala.io.Source.fromInputStream(stream)("UTF-8").getLines

  private val emojis: List[EmojiData] = {
    (for {
      line <- lines.drop(1)
      values = line.split(';').map(_.trim.replaceAll("^\"|\"$", ""))
    } yield EmojiData(values(0), values(1), values(2), values(3))).toList
  }

  def isEmoji(word: String): Boolean = emojis.exists(e => e.native == word)

  def getEmojiString(word: String): String = {
    val emojiData = emojis.find(e => e.native.equals(word))
    if (emojiData.isDefined) {
      emojiData.get.text.toLowerCase
    } else {
      word
    }
  }
}
