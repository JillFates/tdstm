class RefCodeTagLib {

    static namespace = "refcode"

    def refCodeService
    
    /**
     * Renders a HTML select for the given domain
     * 
     * Attributes:
     * 
     * domain - the domain name
     * 
     * Examples:
     * 
     * <refcode:select domain="orderStatus" />
     * <refcode:select domain="orderStatus" noSelection="['':'']" /> 
     **/    
    def select = { attrs ->
        if (!attrs.domain) {
            throwTagError("Tag [refcode:select] is missing required attribute [domain]")
        }

        def domain = attrs.remove('domain')
        def displayType = attrs.remove('displayType')
        def optionValue
        if (displayType == 'value') {
            optionValue = 'value'
        }
        else if (displayType == 'abbreviation') {
            optionValue = 'abbreviationOrValue'
        }
        else {
            optionValue = 'meaningOrValue'
        }
        
        attrs.from = refCodeService.get(domain)
        attrs.optionKey = 'value'
        attrs.optionValue = optionValue
        
        out << g.select(attrs)
    }

    /**
     * Renders the abbreviation for the given domain and value 
     * 
     * Attributes:
     * 
     * domain - the domain name
     * value - the value
     * 
     * Examples:
     * 
     * <refcode:abbreviation domain="orderStatus" value="1" />
     * <refcode:abbreviation domain="orderStatus" value="${order.status}"/>
     **/ 
    def abbreviation = { attrs ->
        if (!attrs.domain) {
            throwTagError("Tag [refcode:abbreviation] is missing required attribute [domain]")
        }
        
        if (attrs.value) {
            def rv = refCodeService.get(attrs.domain, attrs.value)
            if (rv) {
                out << rv.abbreviationOrValue
            }
        }
    }
    
    /**
     * Renders the meaning for the given domain and value 
     * 
     * Attributes:
     * 
     * domain - the domain name
     * value - the value
     * 
     * Examples:
     * 
     * <refcode:meaning domain="orderStatus" value="1" />
     * <refcode:meaning domain="orderStatus" value="${order.status}"/>
     **/ 
    def meaning = { attrs ->
        if (!attrs.domain) {
            throwTagError("Tag [refcode:meaning] is missing required attribute [domain]")
        }
        
        if (attrs.value) {
            def rv = refCodeService.get(attrs.domain, attrs.value)
            if (rv) {
                out << rv.meaningOrValue
            }
        }
    }
}
