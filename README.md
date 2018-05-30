# Vader Sentiment in Scala with Spark [![Build Status](https://travis-ci.org/witlox/sentiment-utils.svg?branch=master)](https://travis-ci.org/witlox/sentiment-utils) [![Coverage Status](https://coveralls.io/repos/github/witlox/sentiment-utils/badge.svg?branch=master)](https://coveralls.io/github/witlox/sentiment-utils?branch=master)

This is a Scala conversion of [https://github.com/cjhutto/vaderSentiment](https://github.com/cjhutto/vaderSentiment).  
It contains UDF wrappers for the functions, and outputs a map of the sentiments corresponding to a text.  
Build with Java 1.8, Scala 2.11.8, SBT 0.13.15 and Spark 2.1

Map => { positivity polarity, negative polarity, neutral polarity, compound polarity }

_Compound polarity:_
* positive sentiment: compound score >= 0.5
* neutral sentiment: (compound score > -0.5) and (compound score < 0.5)
* negative sentiment: compound score <= -0.5

Direct calling in Scala:
```scala
SentimentIntensityAnalyser.polarityScores("sentimental text here") 
``` 

or as UDF:

```scala
import io.witlox.sentiment.Vader._

...

// using a UDF you would have to unwrap the map to it's respective columns, here the functions
// are split by element of the map, so simply add them column by column

val dfWithPositiveSentiment = df.withColumn("positive", positive($"text"))

val dfWithCompoundSentiment = dfWithPositiveSentiment.withColumn("sentiment", compound($"text"))

```

Also included is a bucketing function for dates. In order to group sentiments (for example in Tweets) we need to group them.

```scala
import io.witlox.utils.TimePartition

// create a TimePartition on year (smallest available is milliseconds)
val tp = TimePartition("year")

val bucket = tp.bucketize("2012-01-01T01:01:01.000Z")

```

or as UDF:

```scala
val tp = TimePartition("year")

val dfWithBucket = df.withColumn("bucket", tp.bucket($"ISODateTimeFormatString"))

```
