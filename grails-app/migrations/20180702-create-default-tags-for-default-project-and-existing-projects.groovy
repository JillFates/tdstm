/**
 * @author ecantu
 * Creates four default tags, and associates them to the Default Project, so each time a new Project is created, this
 * four tags are cloned into the new Project.
 * Also it assigns this four tags to the existing Projects for consistency.
 * See TM-10847 - Include Tags from the Default Project when creating a New Project
 */

databaseChangeLog = {

    changeSet(author: "ecantu", id: "20180702 TM-10847-1") {
        comment("Create default Tags for Default Project and existing Projects")
        grailsChange {
            change {
                List projectIds = sql.rows('select p.project_id from project p;')
                List ids = projectIds.collect{it.project_id.toString()}

                for (projectId in ids) {
                    println "Project id: ${projectId}"
                    sql.execute(""" insert INTO tag (version, color, description, name, project_id, date_created)
                            values (0, 'Yellow', 'General Data Protection Regulation Compliance', 'GDPR', ${projectId}, UTC_TIMESTAMP())""")
                    sql.execute(""" insert INTO tag (version, color, description, name, project_id, date_created)
                            values (0, 'Yellow', 'Health Insurance Portability and Accountability Act Compliance', 'HIPPA', ${projectId}, UTC_TIMESTAMP())""")
                    sql.execute(""" insert INTO tag (version, color, description, name, project_id, date_created)
                            values (0, 'Yellow', 'Payment Card Industry Data Security Standard Compliance', 'PCI', ${projectId}, UTC_TIMESTAMP())""")
                    sql.execute(""" insert INTO tag (version, color, description, name, project_id, date_created)
                            values (0, 'Yellow', 'Sarbanes–Oxley Act Compliance', 'SOX', ${projectId}, UTC_TIMESTAMP())""")

                }
            }

        }
    }
}
