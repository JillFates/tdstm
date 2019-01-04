databaseChangeLog = {
	changeSet(author: 'oluna', id: '20180629 TM-9749-1') {
		comment('Add sampleFilename column for DataScript')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'data_script', columnName: 'sample_filename' )
			}
		}

		addColumn(tableName: 'data_script') {
			column(name: 'sample_filename', type: 'VARCHAR(255)', defaultValue: '')
		}
	}

	changeSet(author: 'oluna', id: '20180629 TM-9749-2') {
		comment('Add originalSampleFilename column for DataScript')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'data_script', columnName: 'original_sample_filename' )
			}
		}

		addColumn(tableName: 'data_script') {
			column(name: 'original_sample_filename', type: 'VARCHAR(255)', defaultValue: '')
		}
	}

}
