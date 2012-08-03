class EavModelGrailsPlugin {
	def version = 0.1
	def dependsOn = [:]

	// TODO Fill in these fields
	def author = "John Martin"
	def authorEmail = "jmartin@transitionaldata.com"
	def title = "EAV Model - Entity Attribute Model"
	def description = '''\
The EAV plugin provide an extensible way to develop applications that utilize
the Entity Attribute data model.
'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/EavModel+Plugin"

	def doWithSpring = {
		// TODO Implement runtime spring config (optional)
	}
   
	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)
	}

	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional)
	}

	def getExpandoClassName (claz) {
		// theClass="class Xyzzy"
		return claz.theClass.name
	}

/*	
	def doWithDynamicMethods = { ctx ->

    	println "EavModel Injecting methods on Entity domain classes"

    	def classLoader = application.classLoader
		//SessionFactory sessionFactory = ctx.sessionFactory
		//def insertMethod = new InsertPersistentMethod(sessionFactory, classLoader, application)

		for (dc in application.domainClasses) {
			MetaClass metaClass = dc.metaClass
			def className = getExpandoClassName(metaClass)
			println "Class " + metaClass.theClass

			def isEavModel = false
			metaClass.interfaces.each {
				if (className == "Asset") println it.dump()
				def cname = getExpandoClassName(it)
				println "Interface ${cname}"
				if (cname == "EavEntity") isEavModel = true
			}
			if (isEavModel) {
				println "We found a class that implements EavEntity"
			}

			metaClass.'static'.insert = {Boolean validate ->
				println "CAS Severn, insert validate arg called with ${delegate}"
				insertMethod.invoke(delegate, "insert", [validate] as Object[])
			}
			metaClass.'static'.insert = {Map args ->
				println "CAS Severn, insert map-args called with ${delegate}"
				insertMethod.invoke(delegate, "insert", [args] as Object[])
			}
			metaClass.'static'.insert = {->
				println "CAS Severn, insert no-args called with ${delegate}"
				if (!delegate.validate()) {
					return false
				}
				insertMethod.invoke(delegate, "insert", [] as Object[])
			}
		}
	}
*/
	
	def onChange = { event ->
		// TODO Implement code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}
}
