package org.github.triman.roomba.simulator.utils

import javax.xml.parsers.SAXParserFactory

object NonValidatingSAXParserFactory {
	def getInstance = {
		val f = SAXParserFactory.newInstance
		f.setValidating(false)
		f.setNamespaceAware(false)
		f.setFeature("http://xml.org/sax/features/validation", false)
		f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
		f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
		f.setFeature("http://xml.org/sax/features/external-general-entities", false)
		f.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
		f.newSAXParser
	}
}