import org.springframework.beans.SimpleTypeConverter
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
class PersonRoleSelectTagLib {
	def PartyRelationshipService
    static namespace = 'tds'
    def out // to facilitate testing 
    

    /**
     * A helper tag for Selecting Roles
     *
     * Examples:
     * 
     */
    def personRoleSelect = { attrs ->
        def messageSource = grailsAttributes.applicationContext.getBean( 'messageSource' )
        def locale = RCU.getLocale( request )
        def writer = out
        attrs.id = attrs.id ? attrs.id : attrs.name
        def from = attrs.remove( 'from' )
        def keys = attrs.remove( 'keys' )
        def optionKey = attrs.remove( 'optionKey' )
        def optionValue = attrs.remove( 'optionValue' )
        def value = attrs.remove( 'value' )
        if ( value instanceof Collection && attrs.multiple == null ) {
            attrs.multiple = 'multiple'
        }
        def valueMessagePrefix = attrs.remove( 'valueMessagePrefix' )
        def noSelection = attrs.remove( 'noSelection' )
        def isNew = attrs.remove( 'isNew' )
        def currentRole = attrs.remove( 'currentRole' )
        // Override the Value with current value
        if ( currentRole )
        {
        	value = attrs.remove( 'currentRole')
        }
        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }
        def disabled = attrs.remove( 'disabled' )
        if ( disabled && Boolean.valueOf( disabled ) ) {
            attrs.disabled = 'disabled'
        }

        writer << "<select name=\"${attrs.remove( 'name' )}\" "
        // process remaining attributes
        outputAttributes( attrs )

        writer << '>'
        writer.println()

        if ( noSelection ) {
        	renderNoSelectionOption( noSelection.key, noSelection.value, value )
            writer.println()
        }

        // If isNew=="true" Insert a default option to select box
        if ( isNew && isNew == "true" )
        {
        	writer << '<option '
        	writeValueAndCheckIfSelected( "", null, writer )
        	writer << '>'
        	def message = "Please Select"
            if (message != null) {
                writer << message.encodeAsHTML()
            }
            writer << '</option>'
        }
      
        //      End of isNew=="true" Insert a default option to select box
        //      create options from list
        if ( from ) {
            from.eachWithIndex {el, i ->
                def keyValue = null

                writer << '<option '
                if ( keys ) {
                	
                    keyValue = keys[i]
                    //                  Checking for Cuurent Role option To Select
                    if ( currentRole == keyValue.toString() )
                    {
                    	
                    	writeValueAndCheckIfSelected(keyValue, keyValue, writer)
                    }
                    else
                    writeValueAndCheckIfSelected(keyValue, value, writer)
                  
                    	
                }
                else if ( optionKey ) {
                    if ( optionKey instanceof Closure ) {
                        keyValue = optionKey( el )
                    }
                    else if ( el != null && optionKey == 'id' && grailsApplication.getArtefact( DomainClassArtefactHandler.TYPE, el.getClass().name ) ) {
                        keyValue = el.ident()
                    }
                    else {
                        keyValue = el[optionKey]
                    }
                    //Checking for Cuurent Role option To Select
                    if ( currentRole == keyValue.toString() )
                    {
                    	
                    	writeValueAndCheckIfSelected(keyValue, keyValue, writer)
                    }
                    else
                    writeValueAndCheckIfSelected(keyValue, value, writer)
                  
                }
                else {
                    keyValue = el
                    
                    //Checking for Cuurent Role option To Select
                    if ( currentRole == keyValue.toString() )
                    {
                    	
                    	writeValueAndCheckIfSelected(keyValue, keyValue, writer)
                    }
                    else
                    writeValueAndCheckIfSelected(keyValue, value, writer)
                   
                }
                
                writer << '>'
               
                if (valueMessagePrefix) {
                    def optionValueStr = optionValueToString(el, optionValue)
                    def messageArgs = optionValue ? optionValueStr : keyValue
                    def message = messageSource.getMessage("${valueMessagePrefix}.${messageArgs}", null, null, locale)
                    if (message != null) {
                        writer << message.encodeAsHTML()
                    }
                    else if (optionValue) {
                        writer << optionValueStr
                    }
                    else if (keyValue) {
                        writer << keyValue.encodeAsHTML()
                    }
                    else {
                        def s = el.toString()
                        if (s) writer << s.encodeAsHTML()
                    }
                }
                else if (optionValue) {
                    writer << optionValueToString(el, optionValue)
                }
                else {
                    def s = el.toString()
                    if (s) writer << s.encodeAsHTML()
                }

                writer << '</option>'
                writer.println()
            }
        }
        // close tag
        writer << '</select>'
        
    }


    def typeConverter = new SimpleTypeConverter()

    private writeValueAndCheckIfSelected(keyValue, value, writer) {
        boolean selected = false
       
        def keyClass = keyValue?.getClass()
        if (keyClass.isInstance(value)) {
        	
            selected = (keyValue == value)
        }
        else if (value instanceof Collection) {
            selected = value.contains(keyValue)
        }
        else if (keyClass && value) {
            try {
                value = typeConverter.convertIfNecessary(value, keyClass)
                selected = (keyValue == value)
            } catch (Exception) {
                // ignore
            }
        }
        writer << "value=\"${keyValue}\" "
        if (selected) {
            writer << 'selected="selected" '
        }
    }


    /**
     * Dump out attributes in HTML compliant fashion
     */
    void outputAttributes(attrs) {
        attrs.remove( 'tagName') // Just in case one is left
        attrs.each {k, v ->
            out << k << "=\"" << v?.encodeAsHTML() << "\" "
        }
    }

    def renderNoSelectionOption = {noSelectionKey, noSelectionValue, value ->
        // If a label for the '--Please choose--' first item is supplied, write it out
        out << '<option value="' << (noSelectionKey == null ? "" : noSelectionKey) << '"'
        if (noSelectionKey.equals( value)) {
            out << ' selected="selected" '
        }
        out << '>' << noSelectionValue.encodeAsHTML() << '</option>'
    }

    private String optionValueToString( def el, def optionValue) {
        if ( optionValue instanceof Closure) {
            return optionValue( el).toString().encodeAsHTML()
        }

        //el[optionValue].toString().encodeAsHTML()
    }
    
    /*staffSelect Tag Library
     *
     *
     */
    def staffSelect  = { attrs ->
        
    	def messageSource = grailsAttributes.applicationContext.getBean( 'messageSource' )
    	def locale = RCU.getLocale( request )
    	def writer = out
    	attrs.id = attrs.id ? attrs.id : attrs.name
    	def from = attrs.remove( 'from' )
    	def keys = attrs.remove( 'keys' )
    	def optionKey = attrs.remove( 'optionKey' )
    	def optionValue = attrs.remove( 'optionValue' )
    	def value = attrs.remove( 'value' )
    	if ( value instanceof Collection && attrs.multiple == null ) {
    		attrs.multiple = 'multiple'
    	}
    	def valueMessagePrefix = attrs.remove( 'valueMessagePrefix' )
    	def noSelection = attrs.remove( 'noSelection' )
    	def isNew = attrs.remove( 'isNew' )
        def companyId = attrs.remove( 'companyId' )
        
    	if (noSelection != null) {
    		noSelection = noSelection.entrySet().iterator().next()
    	}
    	def disabled = attrs.remove( 'disabled' )
    	if ( disabled && Boolean.valueOf( disabled ) ) {
    		attrs.disabled = 'disabled'
    	}

    	writer << "<select name=\"${attrs.remove( 'name' )}\" "
    	// process remaining attributes
    	outputAttributes( attrs )

    	writer << '>'
    	writer.println()
    	
    	if ( noSelection ) {
    		renderNoSelectionOption( noSelection.key, noSelection.value, value )
    		writer.println()
    	}

    	
        // If isNew=="true" Insert a default option to select box
    	if ( isNew && isNew == "true" )
    	{
    		writer << '<option '
    		writeValueAndCheckIfSelected( "", null, writer )
    		writer << '>'
    		def message = "Please Select"
        	if (message != null) {
        		writer << message.encodeAsHTML()
        	}
    		writer << '</option>'
    	}
    	
        // End of isNew=="true" Insert a default option to select box
        from = partyRelationshipService.getCompanyStaff( companyId )
    		
        // create options from list
    	if ( from ) {
    		from.eachWithIndex {el, i ->
                def keyValue = null

                writer << '<option '
                if ( keys ) {
            	
                    keyValue = keys[i]
                    //                  Checking for Cuurent Role option To Select
                
                    writeValueAndCheckIfSelected(keyValue, value, writer)
               	
                }
                else if ( optionKey ) {
                    if ( optionKey instanceof Closure ) {
                        keyValue = optionKey( el )
                    }
                    else if ( el != null && optionKey == 'id' && grailsApplication.getArtefact( DomainClassArtefactHandler.TYPE, el.getClass().name ) ) {
                        keyValue = el.ident()
                    }
                    else {
                        keyValue = el[optionKey]
                    }
                
                    writeValueAndCheckIfSelected(keyValue, value, writer)
              
                }
                else {
                    keyValue = el
                
                    writeValueAndCheckIfSelected(keyValue, value, writer)
               
                }
            
                writer << '>'
           
                if (valueMessagePrefix) {
                    def optionValueStr = optionValueToString(el, optionValue)
                    def messageArgs = optionValue ? optionValueStr : keyValue
                    def message = messageSource.getMessage("${valueMessagePrefix}.${messageArgs}", null, null, locale)
                    if (message != null) {
                        writer << message.encodeAsHTML()
                    }
                    else if (optionValue) {
                        writer << optionValueStr
                    }
                    else if (keyValue) {
                        writer << keyValue.encodeAsHTML()
                    }
                    else {
                        def s = el.toString()
                        if (s) writer << s.encodeAsHTML()
                    }
                }
                else if (optionValue) {
                    writer << optionValueToString(el, optionValue)
                }
                else {
                    def s = el.toString()
                    if (s) writer << s.encodeAsHTML()
                }

                writer << '</option>'
                writer.println()
            }
        }
        // close tag
        writer << '</select>'
    
    }

}