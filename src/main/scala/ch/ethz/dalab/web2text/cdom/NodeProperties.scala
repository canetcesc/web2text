package ch.ethz.dalab.web2text.cdom

import org.jsoup.{nodes => jnodes}
import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.utilities.Settings

/** Node properties
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
class NodeProperties (
  var nCharacters: Int,
  var nWords: Int,
  var nSentences: Int,
  var nPunctuation: Int,
  var nNumeric: Int,
  var nDashes: Int,
  var nStopwords: Int,
  var containsPopularName: Boolean,
  var containsAuthorParticle: Boolean,
  var nWordsWithCapital: Int,
  var nCharsInLink: Int,
  var totalWordLength: Int,
  var endsWithPunctuation: Boolean,
  var endsWithQuestionMark: Boolean,
  var startPosition: Int,
  var endPosition: Int,
  var nChildrenDeep: Int,
  var containsCopyright: Boolean,
  var containsEmail: Boolean,
  var containsUrl: Boolean,
  var containsAuthorUrl: Boolean,
  var containsYear: Boolean,
  var blockBreakBefore: Boolean,
  var blockBreakAfter: Boolean,
  var brBefore: Boolean,
  var brAfter: Boolean,
  var containsForm: Boolean,
  var containsAuthor: Boolean
) {


  def toHTML: String = s"""
  |<dl>
  |  <dt>nCharacters</dt><dd>$nCharacters</dd>
  |  <dt>nWords</dt><dd>$nWords</dd>
  |  <dt>nSentences</dt><dd>$nSentences</dd>
  |  <dt>nPunctuation</dt><dd>$nPunctuation</dd>
  |  <dt>nNumeric</dt><dd>$nNumeric</dd>
  |  <dt>nDashes</dt><dd>$nDashes</dd>
  |  <dt>nStopwords</dt><dd>$nStopwords</dd>
  |  <dt>containsPopularName</dt><dd>$containsPopularName</dd>
  |  <dt>containsAuthorParticle</dt><dd>$containsAuthorParticle</dd>|
  |  <dt>nWordsWithCapital</dt><dd>$nWordsWithCapital</dd>
  |  <dt>nCharsInLink</dt><dd>$nCharsInLink</dd>
  |  <dt>containsCopyright</dt><dd>$containsCopyright</dd>
  |  <dt>totalWordLength</dt><dd>$totalWordLength</dd>
  |  <dt>endsWithPunctuation</dt><dd>$endsWithPunctuation</dd>
  |  <dt>endsWithQuestionMark</dt><dd>$endsWithQuestionMark</dd>
  |  <dt>startPosition</dt><dd>$startPosition</dd>
  |  <dt>endPosition</dt><dd>$endPosition</dd>
  |  <dt>nChildrenDeep</dt><dd>$nChildrenDeep</dd>
  |  <dt>blockBreakBefore</dt><dd>$blockBreakBefore</dd>
  |  <dt>blockBreakAfter</dt><dd>$blockBreakAfter</dd>
  |  <dt>brBefore</dt><dd>$brBefore</dd>
  |  <dt>brAfter</dt><dd>$brAfter</dd>
  |  <dt>containsForm</dt><dd>$containsForm</dd>
  |  <dt>containsAuthor</dt><dd>$containsAuthor</dd>
  |</dl>""".stripMargin
}


/** Factory for the [[ch.ethz.dalab.web2text.cdom.NodeProperties]] class */
object NodeProperties {

  /** Create a NodeProperties object from a domnode and its children.
    *
    * If there are no children, it is about a leaf
    * If there is one child, it merges the features,
    * and if there are multiple children, add up the stuff (in most cases)
    */
  def fromNode(domnode: jnodes.Node, children: Seq[Node]): NodeProperties =
    children.length match {

      // If there are no children, this is a leaf
      case 0 => {

        val s = Settings
        val text = DOM.text(domnode).trim
        val words = text.split("\\W+").filter(x => x.length > 0)
        val sentences = Util.splitSentences(text)

        val regexEmail = """\b(?=[^\s]+)(?=(\w+)@([\w\.]+))\b""".r
        val regexUrl   = """\b(https?|ftp)://[^\s/$.?#].[^\s]*\b""".r
        val regexYear  = """\b\d{4}\b""".r
        val regexAuthorUrl = """\b(https?|ftp)://.+/author/\b""".r

        //System.out.println("NodeProperies - Settings.author=" + s.author)

        new NodeProperties(
          nCharacters           = scala.math.max(text.length,1),
          nWords                = words.length,
          nSentences            = sentences.length,
          nPunctuation          = text.count { x => s.punctuation.contains(x) },
          nNumeric              = text.count { _.isDigit },
          nDashes               = text.count { x => s.dashes.contains(x) },
          nStopwords            = words.count { x => s.stopwords.contains(x) },
          containsPopularName   = (words.length >=2 && words.length <= 5  &&
                                  words.count { _.charAt(0).isUpper }>0) &&
                                  words.count { x => s.popularNames.contains(x.toLowerCase()) } > 0,
          containsAuthorParticle = (words.length >=2 && words.length <= 5) &&
                                  words.count { x => s.authorParticle.contains(x.toLowerCase()) } > 0 ,
          nWordsWithCapital     = words.count { _.charAt(0).isUpper },
          nCharsInLink          = if (domnode.nodeName == "a") text.length else 0,
          totalWordLength       = words.view.map(_.length).sum,
          endsWithPunctuation   = if (text.length == 0) false
                                  else s.punctuation.contains(text.last),
          endsWithQuestionMark  = if (text.length == 0) false
                                  else (text.last == '?'),
          startPosition         = domnode.startPosition,
          endPosition           = domnode.endPosition,
          nChildrenDeep         = 0,
          containsCopyright     = text.contains("©") || text.contains("©️"),
          containsEmail         = regexEmail.findFirstIn(text) != None,
          containsUrl           = regexUrl.findFirstIn(text) != None,
          containsAuthorUrl     = regexAuthorUrl.findFirstIn(text) != None,
          containsYear          = regexYear.findFirstIn(text) != None,
          blockBreakBefore      = domnode.previousSibling != null &&
                                  Settings.blockTags.contains
                                   (domnode.previousSibling.nodeName),
          blockBreakAfter       = domnode.nextSibling != null &&
                                  Settings.blockTags.contains
                                   (domnode.nextSibling.nodeName),
          brBefore              = domnode.previousSibling != null &&
                                  domnode.previousSibling.nodeName == "br",
          brAfter               = domnode.nextSibling != null &&
                                  domnode.nextSibling.nodeName == "br",
          containsForm          = if (domnode.isInstanceOf[jnodes.Element])
                                    !domnode.asInstanceOf[jnodes.Element].getElementsByTag("input").isEmpty
                                  else false,
          containsAuthor        = words.length >=2 && words.length <= 5 && text.contains(s.author)
        )
      }

      // If there is one child, merge the features
      case 1 => {

        var cfeat = children.head.properties
        val tag = domnode.nodeName
        val prevtag = Option(domnode.previousSibling).map(_.nodeName) getOrElse "[none]"
        val nexttag = Option(domnode.nextSibling).map(_.nodeName) getOrElse "[none]"

        assert(
          cfeat != null,
          "We cannot initialize features from a child, if the child doesn't have them"
        )

        val blockBreakBefore = Settings.blockTags.contains(tag) || Settings.blockTags.contains(prevtag)
        val blockBreakAfter  = Settings.blockTags.contains(tag) || Settings.blockTags.contains(nexttag)
        if (blockBreakBefore) {
          propagateDownBlockTagLeft(children)
        }
        if (blockBreakAfter) {
          propagateDownBlockTagRight(children)
        }

        new NodeProperties(
          nCharacters           = cfeat.nCharacters,
          nWords                = cfeat.nWords,
          nSentences            = cfeat.nSentences,
          nPunctuation          = cfeat.nPunctuation,
          nNumeric              = cfeat.nNumeric,
          nDashes               = cfeat.nDashes,
          nStopwords            = cfeat.nStopwords,
          containsPopularName   = cfeat.containsPopularName,
          containsAuthorParticle   = cfeat.containsAuthorParticle,
          nWordsWithCapital     = cfeat.nWordsWithCapital,
          nCharsInLink          = if (domnode.nodeName == "a")
                                    cfeat.nCharacters
                                  else
                                    cfeat.nCharsInLink,
          totalWordLength       = cfeat.totalWordLength,
          endsWithPunctuation   = cfeat.endsWithPunctuation,
          endsWithQuestionMark  = cfeat.endsWithQuestionMark,
          containsCopyright     = cfeat.containsCopyright,
          containsEmail         = cfeat.containsEmail,
          containsUrl           = cfeat.containsUrl,
          containsAuthorUrl     = cfeat.containsAuthorUrl,
          containsYear          = cfeat.containsYear,
          startPosition         = if (cfeat.startPosition > -1)
                                    cfeat.startPosition
                                  else
                                    domnode.startPosition,
          endPosition           = if (cfeat.endPosition > -1)
                                    cfeat.endPosition
                                  else
                                    domnode.endPosition,
          nChildrenDeep         = cfeat.nChildrenDeep,
          blockBreakBefore      = blockBreakBefore,
          blockBreakAfter       = blockBreakAfter,
          brBefore              = cfeat.brBefore ||
                                  (domnode.previousSibling != null &&
                                   domnode.previousSibling.nodeName == "br"),
          brAfter               = cfeat.brAfter ||
                                  (domnode.nextSibling != null &&
                                   domnode.nextSibling.nodeName == "br"),
          containsForm          = cfeat.containsForm,
          containsAuthor        = cfeat.containsAuthor
        )
      }

      // If there are multiple children, add up the stuff (in most cases)
      case _ => { // >= 2
        // Collect and rename some variables for convenience
        var cfeat     = children.map(_.properties)
        var tag       = domnode.nodeName
        val prevtag = Option(domnode.previousSibling).map(_.nodeName) getOrElse "[none]"
        val nexttag = Option(domnode.nextSibling).map(_.nodeName) getOrElse "[none]"

        val blockBreakBefore = Settings.blockTags.contains(tag) || Settings.blockTags.contains(prevtag)
        val blockBreakAfter  = Settings.blockTags.contains(tag) || Settings.blockTags.contains(nexttag)
        if (blockBreakBefore) {
          propagateDownBlockTagLeft(children)
        }
        if (blockBreakAfter) {
          propagateDownBlockTagRight(children)
        }

        // Initialize the features to their neural values
        val features = new NodeProperties(
          nCharacters=0, nWords=0, nSentences=0, nPunctuation=0, nNumeric=0, nDashes=0,
          containsCopyright=false, containsEmail=false, containsUrl=false, containsAuthorUrl=false, containsYear=false,
          nStopwords=0,
          containsPopularName=false,
          containsAuthorParticle=false,
          nWordsWithCapital=0, totalWordLength=0, nChildrenDeep=cfeat.length,
          nCharsInLink          = 0,
          endsWithPunctuation   = cfeat.last.endsWithPunctuation,
          endsWithQuestionMark  = cfeat.last.endsWithQuestionMark,
          startPosition         = cfeat.head.startPosition,
          endPosition           = cfeat.last.endPosition,
          blockBreakBefore      = blockBreakBefore,
          blockBreakAfter       = blockBreakAfter,
          brBefore              = prevtag == "br",
          brAfter               = nexttag == "br",
          containsForm          = if (domnode.isInstanceOf[jnodes.Element])
                                    !domnode.asInstanceOf[jnodes.Element].getElementsByTag("input").isEmpty
                                  else false,
          containsAuthor        = false
        )

        // Update the features, by summing up things
        cfeat.foreach(x => {
          features.nCharacters        += x.nCharacters
          features.nWords             += x.nWords
          features.nSentences         += x.nSentences
          features.nPunctuation       += x.nPunctuation
          features.nNumeric           += x.nNumeric
          features.nDashes            += x.nDashes
          features.nStopwords         += x.nStopwords
          features.containsPopularName   = x.containsPopularName || features.containsPopularName
          features.containsAuthorParticle = x.containsAuthorParticle || features.containsAuthorParticle
          features.nWordsWithCapital  += x.nWordsWithCapital
          features.totalWordLength    += x.totalWordLength
          features.nChildrenDeep      += x.nChildrenDeep
          features.nCharsInLink       += (if (tag == "a") x.nCharacters
                                          else x.nCharsInLink)
          features.containsCopyright   = x.containsCopyright || features.containsCopyright
          features.containsEmail       = x.containsEmail || features.containsEmail
          features.containsUrl         = x.containsUrl || features.containsUrl
          features.containsAuthorUrl   = x.containsAuthorUrl || features.containsAuthorUrl
          features.containsYear        = x.containsYear || features.containsYear
          features.containsAuthor      = x.containsAuthor || features.containsAuthor
        })

        features
      }
    }

    /** Set block break to true for a node's children (left), recursively. */
    def propagateDownBlockTagLeft(children: Seq[Node]): Unit = {
      // on the left
      var childs = children
      while(childs.length > 0) {
        val left = childs.head
        if (left.properties.blockBreakBefore == true) return
        left.properties.blockBreakBefore = true
        childs = left.children
      }
    }

    /** Set block break to true for a node's children (right), recursively. */
    def propagateDownBlockTagRight(children: Seq[Node]): Unit = {
      // on the left
      var childs = children
      while(childs.length > 0) {
        val right = childs.last
        if (right.properties.blockBreakAfter == true) return
        right.properties.blockBreakAfter = true
        childs = right.children
      }
    }
}
