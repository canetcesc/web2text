#SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractPageFeatures  public/html/0a1e33912ecfcb994e15caf56e5fc8d796c11e7c.html public/training/output_7_contains_author2"
#SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractPageFeatures  public/html/0a1e33912ecfcb994e15caf56e5fc8d796c11e7c.html public/dom/output_7_"
SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=1024M"  sbt "runMain ch.ethz.dalab.web2text/ExtractCorpusFeatures  public/html/ public/train/"
# SBT_OPTS="-Xms512M -Xmx6G -Xss2M -XX:MaxMetaspaceSize=1024M" sbt "runMain ch.ethz.dalab.web2text.ExtractPageFeatures  public/html/0a0f7c002df93c493ec96ae1a72e241aa0b824be.html publicdom/output_7_"
