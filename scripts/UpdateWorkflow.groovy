import com.tdssrc.eav.*
def jdbcTemplate = ctx.getBean("jdbcTemplate")

/**
 *  Replace the Mover with Sys_admin
 */
def sysAdminQuery = "UPDATE swimlane SET name='SYS_ADMIN', actor_id='Sys Admin' WHERE name in ('MOVER','ENGINEER')"
jdbcTemplate.update(sysAdminQuery)

/**
 *  Add DB_ADMIN to each workflow
 */
def workflows = Workflow.list()

workflows.each{ workflow->
	def dbAdmin = Swimlane.findByNameAndWorkflow("DB_ADMIN",workflow)
	if( !dbAdmin ){
		dbAdmin = new Swimlane( actorId:'DB Admin', 
								name:'DB_ADMIN', 
								Workflow: workflow 
								)
		if ( !dbAdmin.validate() || !dbAdmin.save() ) {
			def etext = "Unable to create Swimlane for ${workflow} : "
			dbAdmin.errors.allErrors.each() {etext += "\n"+it }
			println etext
		}
	}
}

/**
 *  Create Move Tech and Logistics(CLEANER) roles
 */
def moveTechRole = RoleType.findById("MOVE_TECH")
if( !moveTechRole ){
	moveTechRole = new RoleType( description : 'Staff : Move Technician')
	moveTechRole.id = "MOVE_TECH"
	if ( !moveTechRole.validate() || !moveTechRole.save(insert:true,flush:true) ) {
		def etext = "Unable to create moveTech Role : "
		moveTechRole.errors.allErrors.each() {etext += "\n"+it }
		println etext
	}
}

def logisticsRole = RoleType.findById("CLEANER")
if( !logisticsRole ){
	logisticsRole = new RoleType( description : 'Staff : Logistics Technician')
	logisticsRole.id = "CLEANER"
	if ( !logisticsRole.validate() || !logisticsRole.save(insert:true,flush:true) ) {
		def etext = "Unable to create logistics Role : "
		logisticsRole.errors.allErrors.each() {etext += "\n"+it }
		println etext 
	}
}

