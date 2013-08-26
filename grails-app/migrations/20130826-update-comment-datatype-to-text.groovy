
/**
 * This change set is used to update asset_comment table's colmn .
 */
databaseChangeLog = {
	//This change set is used to update asset_comment table's colmn .
	changeSet(author: "lokanada", id: "20130808 TM-2165-5") {
		preConditions(onFail:'MARK_RAN') {
			//Verifying if datatype is varchar then only going ahead and updating datatype 
			sqlCheck(expectedResult:"0", "SELECT IF( (select DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS IC WHERE TABLE_NAME = 'asset_comment' AND COLUMN_NAME = 'comment')='varchar', 0, 1)" )
		}
		
		//updating datatype from varchar to text for assetComment
		modifyDataType(columnName:"comment", newDataType:"text", schemaName:"tdstm", tableName:"asset_comment")
	}
}