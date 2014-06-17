import java.text.DateFormat
import java.text.SimpleDateFormat
import org.apache.commons.validator.UrlValidator
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.HtmlUtil
import org.springframework.beans.SimpleTypeConverter
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

class CustomTagLib {
	static namespace = 'tds'
	
	/**
	 * Used to adjust a date to a specified timezone and format to the default (yyyy-MM-dd  kk:mm:ss) or one specified
	 */
	def convertDate = { attrs ->
		Date dt = attrs['date'];
		def tzId = attrs['timeZone']
		def format = attrs['format']
		
		String dtStr = dt.getClass().getName().toString();
		String dtParam = dt.toString();	
		
		if (dtStr.equals("java.util.Date") || dtStr.equals("java.sql.Timestamp")) {	
			DateFormat formatter ; 
			formatter = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss');
			dt = GormUtil.convertInToUserTZ( dt, tzId )
			dtParam = formatter.format(dt);		
		}  
		/* if null or any plain string */
		out << ""
		if (dtParam != "null") {
			dtParam = dtParam.trim();
			switch(format){
				case "MM/dd" :
					out << dtParam[5..6]+"/"+dtParam[8..9]
					break
				case "MM/dd kk:mm:ss" :
					out << dtParam[5..6]+'/'+dtParam[8..9]+' '+dtParam[11..18]
					break
				case "MM/dd kk:mm" :
					out << dtParam[5..6]+'/'+dtParam[8..9]+' '+dtParam[11..15]
					break
				case "M/d" :
					out << (dtParam[5] =='0' ? dtParam[6] : dtParam[5..6])+'/'+ (dtParam[8] == '0'? dtParam[9]: dtParam[8..9])+'/'+dtParam[0..3]
					break
				case "M/d kk:mm" :
					out << (dtParam[5] =='0' ? dtParam[6] : dtParam[5..6])+'/'+ (dtParam[8] == '0'? dtParam[9]: dtParam[8..9])+'/'+dtParam[0..3]+' '+dtParam[11..15]
					break
				default:
					out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]
					break
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
			} else if(formate == "yyyy/mm-dd hh:mm a"){
				out << dtParam[0..3]+"/"+dtParam[5..6]+"/"+dtParam[8..9]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
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
   	}

	/**
	 * Used to output the elapsed duration between two times in an Ago shorthand
	 * @param Date	a start datetime
	 * @param Date	an ending datetime
	 */
	def elapsedAgo = { attrs ->
		def start = attrs.start
		def end = attrs.end
		
		if ( ! start || ! end ) {
			out << ''
		} else {
			out << TimeUtil.ago(start, end)
		}
	}
	
	/**
	* Used to generate an HTML Action Button 
	* @param label - text to display in Button
	* @param icon - CSS icon to display in button
	* @param id - CSS id to embed into IDs
	* @param onclick - Javascript to add to button
	*/
	def actionButton = { attrs ->
		out << HtmlUtil.actionButton( attrs['label'], attrs['icon'], attrs['id'], attrs['onclick'] )
	}

	/**
	 * Used to output text as URL if it matches or straight text otherwise
	 * @param text - text or URL to be displayed, for URL if there is a pipe (|) character after the URL, then the follow text will be used as the link (required)
	 * @param target - set the A 'target' tag appropriately (optional)
	 * @param class - when presented it will be added to the style if it is a link (optional)
	 *
	 */
	def textAsLink = { attrs ->
		def text = attrs['text']
		def target = attrs['target'] ?: ''
		def css = attrs['class'] ?: ''
		def url
		def label

		String[] schemes = ["http","https","ftp","ftps","smb","file"].toArray();
		UrlValidator urlValidator = new UrlValidator(schemes);
		
		def isUrl = urlValidator.isValid(text)
		
		if (isUrl) {
			def tokens = text.tokenize('|')
			url = tokens[0]
			label = tokens.size() > 1 ? tokens[1] : url
		} else {
			if (text.startsWith("\\\\") || text =~ "[A-z]+:/") {
				isUrl = true
				text = "file://" + text
				def tokens = text.tokenize('|')
				url = tokens[0]
				label = tokens.size() > 1 ? tokens[1] : url
			} 
		}

		if (isUrl) {
			out << "<a href=\"$url\""
			if (target) {
				out << " target=\"$target\""
			}
			if (css) {
				out << " class=\"$css\""
			}
			out << ">$label</a>"
		} else {
			out << text
		}

	}
    
    
	/**
	 * Used to adjust a date to a specified timezone and format to the default (yyyy-MM-dd  kk:mm:ss) or one specified
	 */
	def select = { attrs ->
		def id = attrs['id'];
		def name = attrs['name']
        def clazz = attrs['class']
        def onchange = attrs['ng-change']
        def ngModel = attrs['ng-model']
        def ngShow = attrs['ng-show']
        def ngDisabled = attrs['ng-disabled']
        def datasource = attrs['datasource']
        
        def from = attrs['from']        
		def optionKey = attrs['optionKey']
        def optionValue = attrs['optionValue']
        def noSelection = attrs['noSelection']
        def required = attrs['required']

        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }

        out << "<select "
        if (name) {
            out << "name=\"$name\" "
        }
        if (id) {
            out << "id=\"$id\" "
        }
        if (clazz) {
            out << "class=\"$clazz\" "
        }
        if (onchange) {
            out << "ng-change=\"$onchange\" "
        }
        if (ngModel) {
            out << "ng-model=\"$ngModel\" "
        }
        if (ngShow) {
            out << "ng-show=\"$ngShow\" "
        }
        if (ngDisabled) {
            out << "ng-disabled=\"$ngDisabled\" "
        }
        if (required) {
            out << "required "
        }

        if (from && datasource) {
            def first=true;
            def label;
            out << "ng-options=\"item.v as item.l for item in $datasource  \" "
            
            out << "ng-init=\"$datasource=["
            from.eachWithIndex {el, i ->
                def keyValue = null

                if (optionKey) {
                    if (optionKey instanceof Closure) {
                        keyValue = optionKey(el)
                    } else if (el != null && optionKey == 'id' && grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, el.getClass().name)) {
                        keyValue = el.ident()
                    } else {
                        keyValue = el[optionKey]
                    }
                }  else {
                    keyValue = el
                }
                
                label = ""
                if (optionValue) {
                    if (optionValue instanceof Closure) {
                        label = optionValue(el).toString().encodeAsHTML()
                    } else {
                        label = el[optionValue].toString().encodeAsHTML()
                    }
                } else {
                    def s = el.toString()
                    if (s) label = s.encodeAsHTML()
                }

                if (!first) {
                    out << "," 
                }
                out << "{v:\'$keyValue\',l:\'$label\'} "
                first = false;
            }
             out << "]\""
        }

        out << ">"
        out.println()

        if (noSelection) {
            out << '<option value="' << (noSelection.key == null ? "" : noSelection.key) << '"'
            out << '>' << noSelection.value.encodeAsHTML() << '</option>'
            out.println()
        }

        
        out << "</select>"
        
    }

}
