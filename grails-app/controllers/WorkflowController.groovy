import org.codehaus.groovy.grails.commons.ApplicationHolder

/*------------------------------------------
 * @author : Lokanada Reddy
 * Controller to perform the workflow CRUD operations
 * ----------------------------------------*/
class WorkflowController {
	
	/* Initialize the services */
	def stateEngineService
	/*-----------------------------------------------
	 * Index method for default action
	 *---------------------------------------------*/
	def index = { redirect(action:home,params:params) }
	
	/*-----------------------------------------------
	 *  Will render Workflow data
	 *---------------------------------------------*/
	def home = {
		[ workflowInstanceList: Workflow.list( params ) ]
	}

	/*-----------------------------------------------
	 * @param : workfow
	 * Will render Workflow steps for selected workflow
	 *---------------------------------------------*/
	def workflowList = {
		def workflowId = params.workflow
		def workflowTransitionsList
		def workflow
		if( workflowId ){
			workflow = Workflow.get(params.workflow)
			workflowTransitionsList = WorkflowTransition.findAllByWorkflow( workflow )
		}
		return [workflowTransitionsList : workflowTransitionsList, workflow : workflow ]
	}
	/*-----------------------------------------------
	 * @param : workfow stepId
	 * provide controlle to set the role to change the status.
	 *---------------------------------------------*/
	def workflowRoles = {
			def transitionId = params.workflowTransition
			def workflowTransitionsList
			def workflowTransition
			def browserTest = request.getHeader("User-Agent").contains("MSIE")
			def headerCount =0
			if( transitionId ){
				workflowTransition = WorkflowTransition.get( transitionId )
				if( !browserTest){
					headerCount = generateHeder(workflowTransition.workflow.id)
				}
				workflowTransitionsList = WorkflowTransition.findAllByWorkflow( workflowTransition?.workflow )
			}
			return [workflowTransitionsList : workflowTransitionsList, workflow : workflowTransition?.workflow, browserTest : browserTest,
					headerCount : headerCount]
	}
	/*-----------------------------------------------
	 * @param : workfow
	 * Generate .svg file for vertical text display in FF/ Safari
	 *---------------------------------------------*/
	def generateHeder(def workflowId){
			
			def workflow = Workflow.get( workflowId )
			def tempTransitions = WorkflowTransition.findAllByWorkflow( workflow )
		       
			def svgHeaderFile = new StringBuffer()
			svgHeaderFile.append("<?xml version='1.0' encoding='UTF-8' standalone='no'?>")
			svgHeaderFile.append("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>")
			svgHeaderFile.append("<svg version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>")
			svgHeaderFile.append("<script type='text/javascript'>")
			svgHeaderFile.append("<![CDATA[")
			svgHeaderFile.append("//this will create htmljavascriptfunctionname in html document and link it to changeText")
			svgHeaderFile.append("top.htmljavascriptfunctionname = changeText;")
			svgHeaderFile.append("function changeText(txt){")
			svgHeaderFile.append("targetText=document.getElementById('thetext');")
			svgHeaderFile.append("var newText = document.createTextNode(txt);")
			svgHeaderFile.append("targetText.replaceChild(newText,targetText.childNodes[0]);")
			svgHeaderFile.append("}")
			svgHeaderFile.append("// ]]>")
			svgHeaderFile.append("</script>")
			svgHeaderFile.append("<text id='thetext' text-rendering='optimizeLegibility' transform='rotate(270, 90, 0)' font-weight='bold' "+
								"font-size='12' fill='#333333' x='-11' y='-76' font-family='verdana,arial,helvetica,sans-serif'>")
			def count = 0
			tempTransitions.sort{it.transId}.each{ transition ->
				if(transition.code == "SourceWalkthru" || transition.code == "TargetWalkthru") return 
				def processTransition = transition.name
				def fillColor = transition.header
				if( !fillColor ){
					fillColor = transition.type == "boolean" ? "#FF8000" : "#336600"
				 }
				if(count == 0){
					svgHeaderFile.append("<tspan fill='$fillColor' id='${transition.transId}'>${processTransition}</tspan>")
				} else {
					svgHeaderFile.append("<tspan x='-11' dy='22' fill='$fillColor' id='${transition.transId}'>${processTransition}</tspan>")
				}
				count++
			}
			svgHeaderFile.append("</text>")
			svgHeaderFile.append("<path d='M 22 0 l 0 120")
			def value = 22
			for(int i=0;i<count;i++){
				value = value+22
				svgHeaderFile.append(" M ${value} 0 l 0 120")
			}
			svgHeaderFile.append("' stroke = '#FFFFFF' stroke-width = '1'/>")
			svgHeaderFile.append("</svg>")
			def f = ApplicationHolder.application.parentContext.getResource("templates/headerSvg_workflow.svg").getFile()
			def fop=new FileOutputStream(f)
			if(f.exists()){
				fop.write(svgHeaderFile.toString().getBytes())
				fop.flush()
				fop.close()
			} else {
				println("This file is not exist")
			}
			return count
	}
}
