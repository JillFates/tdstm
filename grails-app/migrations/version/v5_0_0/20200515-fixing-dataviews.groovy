package version.v5_0_0

databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20200515-TM-17718-1') {
		comment('Update system dataviews to to have person_id')
		sql('UPDATE dataview set person_id = null  where is_system=true')
	}

	changeSet(author: 'tpelletier', id: '20200515-TM-17718-2') {
		comment('Update filter for Dependency Analyzer Servers to filter to just servers')

		sql('''UPDATE `tdstm`.`dataview` 
			SET report_schema='{"columns":[{"filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"device","property":"model","width":110,"label":"Model","locked":false},{"filter":"","edit":false,"domain":"device","property":"locationSource","width":110,"label":"Source Location","locked":false},{"filter":"","edit":false,"domain":"device","property":"roomSource","width":110,"label":"Source Room","locked":false},{"filter":"","edit":false,"domain":"device","property":"rackSource","width":110,"label":"Source Rack","locked":false},{"filter":"","edit":false,"domain":"device","property":"locationTarget","width":110,"label":"Target Location","locked":false},{"filter":"","edit":false,"domain":"device","property":"roomTarget","width":110,"label":"Target Room","locked":false},{"filter":"","edit":false,"domain":"device","property":"rackTarget","width":110,"label":"Target Rack","locked":false},{"domain":"device","property":"assetType","width":140,"locked":false,"edit":false,"label":"Device Type","filter":"Server|Appliance|Blade|VM|Virtual"},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","device"],"sort":{"domain":"common","property":"assetName","order":"a"}}'
			WHERE id=9;''')
	}

}
