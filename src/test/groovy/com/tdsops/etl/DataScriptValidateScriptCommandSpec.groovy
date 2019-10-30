package com.tdsops.etl

import grails.databinding.SimpleMapDataBindingSource
import grails.testing.gorm.DataTest
import spock.lang.Specification

class DataScriptValidateScriptCommandSpec extends Specification implements DataTest{

	def dataBinder

	def setup(){
		dataBinder = applicationContext.getBean('grailsWebDataBinder')
	}

	void "test can bind script without trimming content"(){

		given: 'a DataScriptValidateScriptCommand instance'
			final DataScriptValidateScriptCommand command = new DataScriptValidateScriptCommand()

		and: 'a SimpleMapDataBindingSource instance'
			final SimpleMapDataBindingSource source =
				[
					filename: "testETLScript5YIH60bmf0AjAQu6UmGxSkPSYThHmabT.tmp",
					script: """


// Make sure to include the above blank lines
read labels
domain Application
iterate {
    extract 'name' set nameVar
    extract 'name' set nameVar
}
					"""
				]

		when: 'data is bound using a custom binder'
			dataBinder.bind(command, source)

		then: 'result shows script content was not trimmed'
			with(command, DataScriptValidateScriptCommand){
				filename == source.map.filename
				script.readLines().size() == 11
			}

	}
}
