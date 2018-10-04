package net.transitionmanager.service

import grails.transaction.Transactional
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferValue

@Transactional
class DataTransferBatchService {

	/**
	 * Deletes an instance of {@code DataTransferBatch} and {@code DataTransferValue}
	 * @param dataTransferBatch
	 * @return
	 */
	def delete(DataTransferBatch dataTransferBatch) {
		DataTransferValue.executeUpdate("delete from DataTransferValue where dataTransferBatch = ?", [dataTransferBatch])
		dataTransferBatch.delete()
	}
}
