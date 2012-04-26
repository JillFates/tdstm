package com.tdssrc.grails

class WebUtil {
	
	/**
	 * Returns a list of checkbox values as a comma separated string
	 */
	def public static checkboxParamAsString = { param ->
		
		String list = param.collect{ id -> "'"+id.trim()+"'"}.toString()
		list = list.replace("[","").replace("]","")
		
		return list
	}

}
