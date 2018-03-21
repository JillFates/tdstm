databaseChangeLog = {
	changeSet(author: 'jmartin', id: 'TM-TM-9849-01') {
		comment('Concat endpoint_path to endpoint_url credential table and then drop endpoint_path')
		sql("""
			UPDATE api_action set endpoint_url = concat_ws( if( right(endpoint_url,1)<>'/' and left(endpoint_path,1)<>'/', '/',''), endpoint_url, endpoint_path)
			WHERE COALESCE(endpoint_url,'') <> '';
		""")

		sql("""
			ALTER TABLE `api_action` DROP COLUMN `endpoint_path`;
		""")
	}

	changeSet(author: 'jmartin', id: 'TM-TM-9849-02') {
		comment('Add the doc_url column to the api_action table')
		 addColumn(tableName: 'api_action') {
            column(name: 'doc_url', type: 'varchar(255)', defaultValue: '')
        }
	}
}
