package version.v4_6_0

databaseChangeLog = {
	changeSet(author: 'slopez', id: '20181123-TM-13007-1') {
		comment("The New Asset List page titles need to be changed.")
		sql("""
			UPDATE dataview SET name = 'Database List' 			WHERE id = 2;
			UPDATE dataview SET name = 'Device List' 			WHERE id = 3;
			UPDATE dataview SET name = 'Server List' 			WHERE id = 4;
			UPDATE dataview SET name = 'Storage Device List' 	WHERE id = 5;
			UPDATE dataview SET name = 'Logical Storage List' 	WHERE id = 6;
			UPDATE dataview SET name = 'Application List' 		WHERE id = 7;
		""")
	}
}
