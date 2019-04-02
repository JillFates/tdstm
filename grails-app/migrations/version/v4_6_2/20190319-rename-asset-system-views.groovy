package version.v4_6_2

databaseChangeLog = {

	changeSet(author: 'arecordon', id: '20190319 TM-14291-1') {
		comment('Rename Asset System Views')

		grailsChange {
			change {
				Map<Long, String> renameInfoMap = [
					2: 'Databases',
					3: 'Devices',
					4: 'Servers',
					5: 'Physical Storage',
					6: 'Logical Storage',
					7: 'Applications'
				]

				renameInfoMap.each { Long viewId, String newName ->
					sql.executeUpdate("UPDATE dataview SET name=:name WHERE id=:id", [name: newName, id: viewId])
				}
			}
		}
	}
}