package com.tdsops.event

/**
 * A job scheduler event details used to trigger application events of ImportBatch type
 */
class ImportBatchJobSchedulerEventDetails implements Serializable {
	// contains the import batch job project
	Long projectId

	// contains the import batch job batch id
	Long batchId

	// contains the username who queued the import batch job required to assume identity when scheduling secure jobs
	String username

	ImportBatchJobSchedulerEventDetails(Long projectId, Long batchId, String username) {
		this.projectId = projectId
		this.batchId = batchId
		this.username = username
	}
}
