package com.github.takezoe.solr.scala

/**
 * Exception that denotes that an ID was required, but not specified.
 */
class UnspecifiedIdError
  extends Exception("When returning highlights, the ID of the document must be specified")
