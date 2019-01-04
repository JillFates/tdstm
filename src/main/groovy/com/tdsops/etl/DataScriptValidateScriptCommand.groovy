package com.tdsops.etl

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject
import grails.databinding.BindUsing

/**
 * The DataScriptValidateScriptCommand represents the parameters that are necessary to test an ETL script
 */
@Validateable
class DataScriptValidateScriptCommand implements CommandObject {

	/**
	 * Use custom data binding with @BindUsing annotation.
	 * The BindUsing annotation may be used to define a custom binding mechanism for a particular field in a class.<br><br>
	 * It is necessary to maintenance blank lines at the beginning of an ETL script.<br>
	 * Grails data binding has a default behaviour:
	 * <pre>
	 * 	grails.databinding.trimStrings = false
	 * </pre>
	 * https://grails.github.io/grails2-doc/2.5.4/guide/theWebLayer.html#dataBinding
	 * <br><br>
	 * To revert this default behaviour, it is necessary to create a custom data binding.
	 * In this implementation It avoids trim process.
	 */
	@BindUsing({ dataScriptValidateScriptCommand, source ->

		return source['script']
	})
	String script
	String filename

	static constraints = {
		script blank: false
		filename blank: false
	}
}
