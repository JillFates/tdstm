package version.v5_0_0

databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20200515-TM-17718-2') {
		comment('Update filter for Dependency Analyzer Storage to filter by asset type to Storage, and Logical Storage')

		sql('''UPDATE `tdstm`.`dataview` 
				SET report_schema='{"columns":[{" filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"storage","property":"fileFormat","width":110,"label":"Format","locked":false},{"domain":"device","property":"assetType","width":140,"locked":false,"edit":false,"label":"Asset Type","filter":"Logical Storage|Storage"},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","storage","device"],"sort":{"domain":"common","property":"assetName","order":"a"}}'
				WHERE id=12;''')
	}

}
