
/**
 * This change set is used to change column note to datatype text
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20141110 TM-3539-1") {
		comment('This change set is used to change column note to datatype text.')
		preConditions(onFail:'MARK_RAN') {
			//Verifying if datatype is varchar then only going ahead and updating datatype 
			sqlCheck(expectedResult:"0", "SELECT IF( (select DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS IC WHERE TABLE_NAME = 'comment_note' AND COLUMN_NAME = 'note')='varchar', 0, 1)" )
		}
		
		//updating datatype from varchar to text for assetComment
		modifyDataType(columnName:"note", newDataType:"text", schemaName:"tdstm", tableName:"comment_note")
	}
}
