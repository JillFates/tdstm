package com.tdssrc.grails;

/**
 * Created by IntelliJ IDEA.
 * User: John
 * Date: Apr 10, 2009
 * Time: 5:29:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GormUtil {

	def public static allErrorsString = { domain, separator=" : " ->
		def text = ""
		domain?.errors.allErrors.each() { text += separator + it }
		text
	}

}
