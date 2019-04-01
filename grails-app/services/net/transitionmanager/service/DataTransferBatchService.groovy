package net.transitionmanager.service

import grails.gorm.transactions.Transactional
import net.transitionmanager.imports.DataTransferBatch
import net.transitionmanager.imports.DataTransferValue

@Transactional
class DataTransferBatchService {

	/**
	 * Deletes an instance of {@code DataTransferBatch} and {@code DataTransferValue}
	 * @param dataTransferBatch
	 * @return
	 */
	void delete(DataTransferBatch dataTransferBatch) {
		DataTransferValue.executeUpdate("delete from DataTransferValue where dataTransferBatch = ?", [dataTransferBatch])
		dataTransferBatch.delete()
	}

	/**
	 * Saves an instance of {@code DataTransferBatch} using {@code Transactional} annotation
	 * @param dtb an instance of {@code DataTransferBatch}
	 * @return same instance of {@code DataTransferBatch} already saved in database
	 */
	DataTransferBatch save(DataTransferBatch dtb){
		return dtb.save(failOnError: false)
	}
}
