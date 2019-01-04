package net.transitionmanager.command

import net.transitionmanager.command.CommandObject
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Provider




class ETLInfoCommand implements CommandObject  {
	// The provider Id that the was used in the ETL transformation process
	Provider provider

	// The DataScript that was used to process the data
	DataScript dataScript

	// The datetime that the ETL process was performed
	Date dateCreated

	// The person whom invoked the ETL process
	Person createdBy

	// The format that date strings were loaded (default "mm/dd/yyyy")
	String dateFormat = 'mm/dd/yyyy'

	// The filename of the original file that was uploaded to the system (where appliable)
	String originalFilename = ''

	// Flag if blank values should overwrite existing values (default: true)
	Boolean overwriteWithBlanks

	// The indicator that a field should be nulled or cleared out (Default "NULL")
	String nullIndicator = 'NULL'

	// The timezone that the source data was generated in. This is primarily used to support
	// Excel spreadsheets. We'll most likely convert the datetimes strings to GMT
	// so this property wouldn't be necessary
	String timezone = 'GMT'

	// This would be populated with value from Info sheet of TM Export and would
	// warn on changes that were made in TM to assets since the warnOnChangesAfter time
	// It will default to the time that the batch was created otherwise.
	Date warnOnChangesAfter = new Date()

}
