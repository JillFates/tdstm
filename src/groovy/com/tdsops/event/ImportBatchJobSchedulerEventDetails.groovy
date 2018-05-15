package com.tdsops.event

import net.transitionmanager.domain.Project

/**
 * A job scheduler event details used to trigger application events of ImportBatch type
 */
class ImportBatchJobSchedulerEventDetails {
	// contains the import batch job project
	Project project

	// contains the import batch job batch id
	Long batchId

	// contains the username who queued the import batch job required to assume identity when scheduling secure jobs
	String username

	ImportBatchJobSchedulerEventDetails(Project project, Long batchId, String username) {
		this.project = project
		this.batchId = batchId
		this.username = username
	}
}
