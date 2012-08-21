import java.text.DateFormat
import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil
class CustomTagLib {
	static namespace = 'tds'
	
	/**
	 * Used to adjust a date to a specified timezone and format to the default (yyyy-MM-dd  kk:mm:ss) or one specified
	 */
	def convertDate = { attrs ->
		Date dt = attrs['date'];
		def tzId = attrs['timeZone']
		def format = attrs['format'] ?: 'yyyy-MM-dd kk:mm:ss'
		
		String dtStr = dt.getClass().getName().toString();
		String dtParam = dt.toString();	
		
		if (dtStr.equals("java.util.Date")) {	
			DateFormat formatter ; 
			formatter = new SimpleDateFormat(format);
			dt = GormUtil.convertInToUserTZ( dt, tzId )
			dtParam = formatter.format(dt);		
		}  
		/* if null or any plain string */
		if (dtParam != "null") {
		
			dtParam = dtParam.trim();
			if(format=="MM/dd"){
			 out << dtParam[5..6]+"/"+dtParam[8..9]
			} else if (format=="MM/dd hh:mm:ss") {
				out << dtParam[5..6]+'/'+dtParam[8..9]+' '+dtParam[11..18]
			} else {
			 out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]
			}
			
			
		
		}
	}
	/*
	 * Converts a date to User's Timezone and applies formating
	 */
	def convertDateTime = { attrs ->
		Date dt = attrs['date'];
		// TODO : convertDateTime - param formate is misspelled.  Also this should just use the date formatter instead of the multiple if/else conditions
		def formate = attrs['formate'];
		def tzId = attrs['timeZone']
		String dtStr = dt.getClass().getName().toString();
		String dtParam = dt.toString();	
		
		if( dtStr.equals("java.util.Date") || dtStr.equals("java.sql.Timestamp") ){	
			DateFormat  formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
			dt = GormUtil.convertInToUserTZ( dt , tzId )
			dtParam = formatter.format( dt );		
		}  
		/* if null or any plain string */
		if (dtParam != "null") {
			dtParam = dtParam.trim();
			if(formate == "mm/dd"){
				out << dtParam[5..6]+"/"+dtParam[8..9]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
			} else if(formate == "hh:mm"){
				out << dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
			} else {
				out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
			}
		}
	}
	/*
	 * 
	 */
	def truncate = { attrs ->
		String value = attrs['value'];
		if(value){
			def length = value.size()
			if(length > 50){
				out << '"'+value.substring(0,50)+'.."'
			} else {
				out << '"'+value+'"'
			}
		}
	}
	/*
	 * will return the time + GMT as hh:mm AM/PM formate 
	 */
	def convertToGMT = { attrs ->
		Date dt = attrs['date'];
		def offsetTZ =  new Date().getTimezoneOffset() / 60 ;
		String dtStr = dt.getClass().getName().toString();
		String dtParam = dt.toString();	
		// check to see whether the input date is Date object or not
		if( dtStr.equals("java.util.Date") || dtStr.equals("java.sql.Timestamp") ){
			// convert the date into GMT
			//def date = new Date( (Long)(dt.getTime() + (3600000 * offsetTZ)) ) ;
			def date = new Date( (Long)(dt.getTime() + (0 * offsetTZ)) ) ;
			DateFormat formatter ; 
			// convert the date into required formate
			formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
			dtParam = formatter.format(date);		
		}  
		/* if null or any plain string */
		if (dtParam != "null") {
			dtParam = dtParam.trim();
			out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
		}
	}
	/*
	 * Convert seconds into HH:MM format
	 * value should be in seconds
	 */
	def formatIntoHHMMSS = { attrs ->
		def value = attrs['value'];
		if( value ){
			def timeFormate 
    	    def hours = (Integer)(value / 3600 )
    	    	timeFormate = hours >= 10 ? hours : '0'+hours
    	    def minutes = (Integer)(( value % 3600 ) / 60 )
    	    	timeFormate += ":"+(minutes >= 10 ? minutes : '0'+minutes)
			
			out << timeFormate
		}
	}
	
    def sortableLink = { attrs ->
    		def writer = out
    		if(!attrs.property)
    			throwTagError("Tag [sortableColumn] is missing required attribute [property]")
    		if(!attrs.title && !attrs.titleKey)
    			throwTagError("Tag [sortableColumn] is missing required attribute [title] or [titleKey]")

    		def property = attrs.remove("property")
    		def action = attrs.action ? attrs.remove("action") : (params.action ? params.action : "list")

    		def defaultOrder = attrs.remove("defaultOrder")
    		if(defaultOrder != "desc") defaultOrder = "asc"

    		// current sorting property and order
    		def sort = params.sort
    		def order = params.order

    		// add sorting property and params to link params
    		def linkParams = [sort:property]
    		if(params.id) linkParams.put("id",params.id)
    		if(attrs.params) linkParams.putAll(attrs.remove("params"))

    		// determine and add sorting order for this column to link params
    		attrs.class = (attrs.class ? "${attrs.class} sortable" : "sortable")
    		if(property == sort) {
    			attrs.class = attrs.class + " sorted " + order
    			if(order == "asc") {
    				linkParams.order = "desc"
    			}
    			else {
    				linkParams.order = "asc"
    			}
    		}
    		else {
    			linkParams.order = defaultOrder
    		}

    		// determine column title
    		def title = attrs.remove("title")
    		def titleKey = attrs.remove("titleKey")
    		if(titleKey) {
    			if(!title) title = titleKey
    			def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
    			def locale = RCU.getLocale(request)
    			title = messageSource.getMessage(titleKey, null, title, locale)
    		}

    		writer << "${link(action:action, params:linkParams) { title }}"
    	}}
