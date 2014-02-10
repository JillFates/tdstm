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
	
	/**
	 * Returns multi-value String of a List
	 */
	def public static listAsMultiValueString = { param ->
		
		String list = param.toString()
		list = list.replace("[","").replace("]","")
		
		return list
	}
	
	/**
	 * Returns multi-value String of a List
	 */
	def public static listAsPipeSepratedString = { param ->
		
		String list = param.toString()
		list = list.replace("[","").replace("]","").replaceAll(",","|")
		
		return list
	}
	
	/**
	 * this utility method is used to arrange a list in <li> HTML tag to display .
	 * @param reqStrList list of warning strings
	 * @return String enclosed in <li>
	 */
	public static String getListAsli( def reqStrList ){
		def liString = ""
		reqStrList.each{
			liString += "<li>"+it+"</li>"
		}
		return liString
	}
	/** 
	 * this utility method is used to split a camel Case String with "_"
	 * @param string
	 * @return
	 */
	static String splitCamelCase(String s) {
		return s.replaceAll(
		   String.format("%s|%s|%s",
			  "(?<=[A-Z])(?=[A-Z][a-z])",
			  "(?<=[^A-Z])(?=[A-Z])",
			  "(?<=[A-Za-z])(?=[^A-Za-z])"
		   ),
		   "_"
		).toLowerCase();
	 }
	
	/**
	 * Returns multi-value Quoted String of a List
	 */
	def public static listAsMultiValueQuotedString = { param ->
		
		String list = param.collect{ id -> "'"+id.trim()+"'"}.toString()
		list = list.replace("[","").replace("]","")
		
		return list
	}

}
