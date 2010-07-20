import org.codehaus.groovy.grails.validation.ConstrainedProperty

class RefCodeGrailsPlugin {
    def version = "0.2.1"
    def dependsOn = [:]
    def author = "Marcel Overdijk, Jim Shingler"
    def authorEmail = "marceloverdijk@gmail.com, shinglerjim@gmail.com"
    def documentation = "http://grails.org/Ref+Code+Plugin"
	
	def doWithSpring = {
        ConstrainedProperty.registerNewConstraint(
                    RefCodeConstraint.REF_CODE_CONSTRAINT,
                    RefCodeConstraint.class);

    }
	
	def doWithApplicationContext = { appCtx ->
	    //Service are not Autowired into constraints
		//Wire it by hand.
		def refCodeService= appCtx.getBean("refCodeService")
		RefCodeConstraint.refCodeService = refCodeService
	}
}
