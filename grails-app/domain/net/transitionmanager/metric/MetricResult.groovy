package net.transitionmanager.metric

import net.transitionmanager.project.Project

/**
 * MetricResult - represents a single metric that is collected on a daily basis based
 * on the query specified in the associated MetricDefinition. These metrics are captured
 * for each project in the instance where the the Project.recordMetrics is set to 1.
 */
class MetricResult {
	Project project

	// The definition that generated the result
	// MetricDefinition metricDefinition
	String metricDefinitionCode

	// The date that the metric was captured
	Date date

	// The label which consists of the concatenation of the definition group by 
	// property names (e.g. Discovery:Validated) if specified otherwise the 
	// aggregation type (e.g. count, sum, max or min)
	String label

	// The value computed by the query aggregation
	Long value

	static belongsTo = [
		project: Project
		// metricDefinition: MetricDefinition
    ]

	static constraints = {
		project unique: ['metricDefinitionCode', 'date', 'label']
		label size:1..255
	}

	static mapping = {
		version false
		id column: 'project_metric_id'
		date sqlType: 'DATE'
	}
}
