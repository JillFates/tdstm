package com.tdsops.event

import org.springframework.context.ApplicationEvent

/**
 * This class represents an ImportBatchJobSchedulerEvent which encapsulates the details of the
 * import batch required to schedule an import batch through quartz
 */
class ImportBatchJobSchedulerEvent extends ApplicationEvent {

	ImportBatchJobSchedulerEvent(ImportBatchJobSchedulerEventDetails source) {
		super(source)
	}

}
