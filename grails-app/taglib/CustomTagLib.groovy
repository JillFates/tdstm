import com.tdssrc.grails.NumberUtil

import java.text.DateFormat
import java.text.SimpleDateFormat
import org.apache.commons.validator.UrlValidator
import org.apache.commons.codec.net.URLCodec
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.HtmlUtil
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

class CustomTagLib {
	static namespace = 'tds'
	
	/**
	 * Used to adjust a date to a specified timezone and format to the default (yyyy-MM-dd  kk:mm:ss) or one specified
	 * @param date - the date to be formated
	 * @param format - the String format to use to format the date into a string (CURRENTLY NOT USED)
	 * @param endian - the ENDIAN format to use, fallbacks into session then in default
	 * @param mockSession - used by tests to pass in a mock session
	 * @return a date formatted appropriately
	 */
  def convertDate = { attrs ->
    Date dateValue = attrs['date'];
    def format
    def endian = attrs['endian']
    def sessionObj = (attrs.containsKey('mockSession') ? attrs.mockSession : session)
    String dateParamClassName = dateValue.getClass().getName().toString();

    out << ""
    if (dateValue) {
      dateValue.clearTime()
      if (dateParamClassName.equals("java.util.Date") || dateParamClassName.equals("java.sql.Timestamp")) {
        if(!endian) endian = TimeUtil.getUserDateFormat(sessionObj)
		format = TimeUtil.FORMAT_DATE
        DateFormat formatter = TimeUtil.createFormatterForType(endian, format)
        out << TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone, dateValue, formatter)
      }
    }
  }

/* // ^- OLDCODE
  def convertDate = { attrs ->
    Date dateValue = attrs['date'];
    def format = attrs['format']
    def sessionObj = (attrs.containsKey('mockSession') ? attrs.mockSession : session)
    String dateParamClassName = dateValue.getClass().getName().toString();

    out << ""
    if (dateValue) {
      dateValue.clearTime()
      if (dateParamClassName.equals("java.util.Date") || dateParamClassName.equals("java.sql.Timestamp")) {
        DateFormat formatter = TimeUtil.createFormatter(sessionObj, format)
        if (formatter == null) {
          formatter = TimeUtil.createFormatter(sessionObj, TimeUtil.FORMAT_DATE)
        }
        out << TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone, dateValue, formatter)
      }
    }
  }
*/

	/*
	 * Converts a date to User's Timezone and applies formating
	 */
	def convertDateTime = { attrs, body ->
		Date dateValue = attrs['date']
		def format = attrs['format']
		String dateParamClassName = dateValue.getClass().getName().toString();
		def sessionObj = (attrs.containsKey('mockSession') ? attrs.mockSession : session)

		out << ""
		if( dateParamClassName.equals("java.util.Date") || dateParamClassName.equals("java.sql.Timestamp") ){	
			DateFormat formatter = TimeUtil.createFormatter(sessionObj, format)
			if (formatter == null) {
				formatter = TimeUtil.createFormatter(sessionObj, TimeUtil.FORMAT_DATE_TIME)
			}
			out << TimeUtil.formatDateTime(sessionObj, dateValue, formatter)
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
	 * @param text - text or URL to be displayed, for URL if there is a pipe (|) character the pattern will be (label | url) (required)
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
		def isUrl = false

		if (text) { 
			String[] schemes = ['HTTP', 'http','HTTPS', 'https', 'FTP', 'ftp', 'FTPS', 'ftps', 'SMB', 'smb', 'FILE', 'file'].toArray();
			UrlValidator urlValidator = new UrlValidator(schemes);

			// Attempt to split the URL from the label
			def tokens = text.tokenize('|')
			url = tokens.size() > 1 ? tokens[1] : tokens[0]
			label = tokens[0]
			isUrl = urlValidator.isValid(url)
			if (! isUrl) {
				if (url.startsWith('\\\\')) {
					// Handle UNC (\\host\share\file) which needs to be converted to file
					isUrl = true
					url = 'file:' + url.replaceAll('\\\\', '/')
				} else {
					if (( url ==~ /^[A-z]:\\.*/ ) ) {
						// Handle windows directory reference do a drive letter
						isUrl = true
						URLCodec uc = new URLCodec()
						url = 'file://' + uc.encode( url.replace("\\", '/') )
					}
				}
			} 
		}

		if (isUrl) {
			out << /<a href="$url"/
			if (target) {
				out << " target=\"$target\""
			}
			if (css) {
				out << " class=\"$css\""
			}
			out << ">$label</a>"
		} else {
			out << (text?.size()>0 ? text : '')
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

        if(from && datasource) {
        	out << "<option ng-selected=\"{{$ngModel == item.v}}\" value={{item.v}} ng-repeat=\"item in $datasource\">{{item.l}} </option> "
        }

        
        out << "</select>"
        
    }

	/*
	 * Draw an SVG Icon from the source based on the SVG Name
	 * Also apply regex to prevent directory traversal
	 * @param name - name of the svg to show on on icons/svg
	 * @param styleClass - to have more control, it attach a class under tds-svg-icons domain to modify the element as desired
	 * @param width - default as 0 if no provided
	 * @param height - default as 0 if no provided
	 */
	def svgIcon = { attrs ->
		def name = attrs['name']
		def styleClass = attrs['styleClass']
		def height = NumberUtil.toPositiveLong(attrs['height'], 0)
		def width = NumberUtil.toPositiveLong(attrs['width'], 0)
		if(name != '' && name != null) {
			if(styleClass == null) {
				styleClass = ''
			}

			name = name.replaceAll(/\./, "")
			out << "<svg style='${height > 0 ? 'height: ' + height + 'px;' : '' } ${width > 0 ? 'width: ' + width + 'px;' : '' }' class='tds-svg-icons ${styleClass}' viewBox='0 0 115 115' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'> " +
					"<image x='0' y='0' height='110px' width='110px' fill='#1f77b4'  xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='${resource(dir: 'icons/svg', file: name + '.svg')}'></image>" +
					"</svg>"
		}
	}

	/**
	 * Used in the Application show view to show Owner/SMEs name and if the person is not staff of the 
	 * project client then it will include the name of their company as well.
	 * @param client - the company that is the client
	 * @param person - the person to output the name of
	 * @return the person's name and company if staff of project owner or partner
	 *   owner/partner staff -  Robin Banks, Acme
	 *   client staff - Jim Lockar
	 */
	def nameAndCompany = { attrs ->
		def client = attrs.client
		def person = attrs.person
		def personCo = person?.company

		out << (person ? person.toString() : '')
		if (client && personCo && client.id != personCo.id) {
			out << ', ' + personCo.name
		}
	}
}
