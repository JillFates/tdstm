import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")
println "Creating Workflow........."
	
	jdbcTemplate.execute(" DROP TABLE IF EXISTS `workflow_transition_map`")
	jdbcTemplate.execute(" DROP TABLE IF EXISTS `swimlane`")
	jdbcTemplate.execute(" DROP TABLE IF EXISTS `workflow_transition`")
	jdbcTemplate.execute(" DROP TABLE IF EXISTS `workflow`")
							
println """Definition of table `workflow` ....."""
	jdbcTemplate.execute("""CREATE TABLE  `workflow` (
							  `workflow_id` bigint(20) NOT NULL AUTO_INCREMENT,
							  `date_created` datetime NOT NULL,
							  `last_updated` datetime DEFAULT NULL,
							  `process` varchar(255) NOT NULL,
							  `updated_by` bigint(20) DEFAULT NULL,
							  PRIMARY KEY (`workflow_id`),
							  KEY `FK21BD7BF1A1A1F02` (`updated_by`),
							  CONSTRAINT `FK21BD7BF1A1A1F02` FOREIGN KEY (`updated_by`) REFERENCES `person` (`person_id`)
							) ENGINE=MyISAM DEFAULT CHARSET=latin1""")	
println """Definition of table `swimlane` ....."""
	jdbcTemplate.execute("""CREATE TABLE  `swimlane` (
							  `swimlane_id` bigint(20) NOT NULL AUTO_INCREMENT,
							  `actor_id` varchar(255) NOT NULL,
							  `name` varchar(255) NOT NULL,
							  `workflow_id` bigint(20) NOT NULL,
							  PRIMARY KEY (`swimlane_id`),
							  KEY `FKFA8C20746C0F497A` (`workflow_id`),
							  CONSTRAINT `FKFA8C20746C0F497A` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`workflow_id`)
							) ENGINE=MyISAM DEFAULT CHARSET=latin1""")
								
println """Definition of table `workflow_transition` ....."""
	jdbcTemplate.execute("""CREATE TABLE  `workflow_transition` (
							  `workflow_transition_id` bigint(20) NOT NULL AUTO_INCREMENT,
							  `code` varchar(255) NOT NULL,
							  `color` varchar(255) DEFAULT NULL,
							  `dashboard_label` varchar(255) DEFAULT NULL,
							  `header` varchar(255) DEFAULT NULL,
							  `name` varchar(255) NOT NULL,
							  `predecessor` int(11) DEFAULT NULL,
							  `trans_id` int(11) NOT NULL,
							  `type` varchar(255) NOT NULL,
							  `workflow_id` bigint(20) NOT NULL,
							  PRIMARY KEY (`workflow_transition_id`),
							  KEY `FKF88BEDD56C0F497A` (`workflow_id`),
							  CONSTRAINT `FKF88BEDD56C0F497A` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`workflow_id`)
							) ENGINE=MyISAM DEFAULT CHARSET=latin1""")
println """Definition of table `workflow_transition_map` ....."""
	jdbcTemplate.execute("""CREATE TABLE  `workflow_transition_map` (
							  `workflow_transition_map_id` bigint(20) NOT NULL AUTO_INCREMENT,
							  `flag` varchar(255) DEFAULT NULL,
							  `swimlane_id` bigint(20) NOT NULL,
							  `trans_id` int(11) NOT NULL,
							  `workflow_id` bigint(20) NOT NULL,
							  `workflow_transition_id` bigint(20) NOT NULL,
							  PRIMARY KEY (`workflow_transition_map_id`),
							  KEY `FKD2B3CFF26C0F497A` (`workflow_id`),
							  KEY `FKD2B3CFF2771C915A` (`swimlane_id`),
							  KEY `FKD2B3CFF2A6A0B259` (`workflow_transition_id`),
							  CONSTRAINT `FKD2B3CFF2A6A0B259` FOREIGN KEY (`workflow_transition_id`) REFERENCES `workflow_transition` (`workflow_transition_id`),
							  CONSTRAINT `FKD2B3CFF26C0F497A` FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`workflow_id`),
							  CONSTRAINT `FKD2B3CFF2771C915A` FOREIGN KEY (`swimlane_id`) REFERENCES `swimlane` (`swimlane_id`)
							) ENGINE=MyISAM DEFAULT CHARSET=latin1""")
	/*
	 * Load data from xml files and insert into DB.
	 * */
	def filePath = ApplicationHolder.application.parentContext.getResource("/WEB-INF/workflow").file
	// Get file path
	def dir = new File( "${filePath.absolutePath}" )
	def children = dir.list()
	dir.list().each{ fileName->
		if ( fileName.endsWith('.xml') ) {
			
			def xmlFile =  ApplicationHolder.application.parentContext.getResource( "/WEB-INF/workflow/${fileName}" ).getFile()
			def transList = new StringBuffer()
			def xml = new XmlSlurper().parse(xmlFile)
			// create workflow record.....
			def workflow = new Workflow( process : xml.@code.text(), dateCreated : GormUtil.convertInToGMT( "now", "EDT" ) )
			workflow.errors.allErrors.each() { it }
			if( !workflow.hasErrors() && workflow.save(flush:true) ) {
				xml.transitions.each{
					it.transition.each{
						transList.append("(${workflow.id},${it.@id.text()},'${it.@to.text()}','${it.@name.text()}','${it.@type.text()}','${it.@color.text()}', ${it.@predecessor.text() ? it.@predecessor.text() : null},'${it.@dashboardLabel.text()}','${it.@header.text()}'),")
					}
				}
				def transListString = transList.toString()
				/* Insert transition into Workflow Transition table*/
				jdbcTemplate.update("insert into workflow_transition(workflow_id,trans_id,code,name,type,color,predecessor,dashboard_label,header) values "+transListString.substring(0,transListString.lastIndexOf(','))+"");

				// load swimlanes
				def slist = new StringBuffer()
				xml.swimlane.each{ s ->
					s.assignment.each{
						slist.append("(${workflow.id},'${s.@name.text()}','${it.@actor_id.text()}'),")
					}
				}
				
				/* Insert swimlane into Workflow swimlane Map table*/
				jdbcTemplate.update("insert into swimlane(workflow_id, name ,actor_id) values "+slist.toString().substring(0,slist.lastIndexOf(','))+"");
				
				def taskNodesList = new StringBuffer()
				
				def startStates = xml.startState
				startStates.each{startState ->
					def workflowTransition = WorkflowTransition.findByWorkflowAndCode(workflow,"${startState.@name.text()}")
					if(workflowTransition){
						startState.task.each{task->
							def swimlane = Swimlane.findByWorkflowAndName(workflow,"${task.@swimlane.text()}")
			                task.transition.each{ transition ->
								def toTransition = WorkflowTransition.findByWorkflowAndCode(workflow,"${transition.@to.text()}")
								taskNodesList.append("(${workflow.id}, ${workflowTransition.id},${swimlane.id},${toTransition?.transId},'${transition.@flag.text()}'),")
							}
							
						}
					}
				}
				
				def taskNodes = xml.taskNode
				taskNodes.each{tasknode ->
					def workflowTransition = WorkflowTransition.findByWorkflowAndCode(workflow,"${tasknode.@name.text()}")
					if(workflowTransition){
						tasknode.task.each{task->
							def swimlane = Swimlane.findByWorkflowAndName(workflow,"${task.@swimlane.text()}")
			                task.transition.each{ transition ->
								def toTransition = WorkflowTransition.findByWorkflowAndCode(workflow,"${transition.@to.text()}")
								taskNodesList.append("(${workflow.id}, ${workflowTransition.id},${swimlane.id},${toTransition?.transId},'${transition.@flag.text()}'),")
							}
							
						}
					}
				}
				
				/* Insert transition into Workflow Transition Map table*/
				jdbcTemplate.update("insert into workflow_transition_map(workflow_id, workflow_transition_id,swimlane_id, trans_id,flag) values "+taskNodesList.toString().substring(0,taskNodesList.lastIndexOf(','))+"");
				
			}
		}
	}	
