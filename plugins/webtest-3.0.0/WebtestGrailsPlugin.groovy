
class WebtestGrailsPlugin {
	def version = "3.0.0"
	def grailsVersion = "1.3 > *"
	def dependsOn = [core: "1.3 > *"]
    def scopes = [excludes:'war'] 
	def title = "A plug-in that provides functional testing for Grails using Canoo Web Test"
	def author = "Dierk Koenig & Lee Butts"
	def authorEmail = "dierk.koenig at canoo.com & leebutts@gmail.com"
	def description = title
    def documentation = "http://grails.org/plugin/webtest"
}
