package net.transitionmanager.metric

/**
 * MetricDefinition - This domain represents one metric definition that is used to query the
 * application data on a daily basis to gather metrics about the various data points. The
 * definitions are shared across all projects for the TransitionManager instance.
 */
class MetricDefinition {
	// The unique code that represents the metric to be gathered
	String code

	// The mode of the metric
 	String mode

	// The description of the metric
	String description

	// Flag to enable (1) or disable (0) the metric from being collected daily	
	Integer enabled=1
	
	// The JSON containing the meta-data about how the metric should query the database
	String definition = '{}'

	// Reference is managed as a Bag so Hibernate doesn't load all into memory
	// Collection metricResults

	Date dateCreated 
	Date lastUpdated

	static hasMany = {
	//	metricResults: MetricResult
	}

	static constraints = {
		code size: 1..255, unique: true
		description blank:true, size:0..255
		enabled range: 0..1
		definition range: 2..65535
	}

	static mapping = {
		id column: 'metric_definition_id'
		definition sqltype: 'TEXT'
	//	metricResults cascade: 'delete'
	}
}
