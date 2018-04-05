/**
 * @author ecantu
 * TM-10091 Create migration script to rename DataScripts sharing the same name
 * This migration script is related to 20180302-Dataview-add-unique-constraint.groovy.
 * It fixes a problem when the views with the same name that should be renamed,
 * differ from each other in a trailing/leading space or tab character.
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "TM-10091-1") {
		comment("If there are already duplicated records for the same name + it.id, rename before applying the constraint." +
				"Take into account spaces and tabs.")
		grailsChange{
			change {
				sql.execute('SET @ROWNUM=0;')
				def duplicatedDataViews = sql.rows('''
					SELECT id, project_id, trim(replace(replace(replace(replace(name,'\\t',''),'\\r',''),'\\n',''),'\\f','')) AS DV_NAME, @ROWNUM:=@ROWNUM+1 AS ROWNUM
					FROM dataview
					GROUP  BY project_id, DV_NAME
					HAVING COUNT(*) > 1;
				''')

				if (duplicatedDataViews) {
					duplicatedDataViews.each { duplicated ->
						sql.execute("UPDATE dataview SET name = CONCAT(${duplicated.DV_NAME}, ' ', '${duplicated.ROWNUM}') WHERE id = '${duplicated.id}' AND project_id = ${duplicated.project_id}")
					}
				}
			}
		}
	}
}
