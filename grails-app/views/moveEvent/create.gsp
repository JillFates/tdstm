<%@page import="net.transitionmanager.domain.MoveEvent" %>
<%@page import="net.transitionmanager.domain.Rack" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Create Event</title>

    <script type="text/javascript">
      function initialize(){

      // This is called when the page loads to initialize Managers
      $('#moveManagerId').val('${moveManager}')
      $('#projectManagerId').val('${projectManager}')

      }
      function isValidDate( date ){
        var returnVal = true;
      	var objRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
      	if( date && !objRegExp.test(date) ){
          	alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
          	returnVal  =  false;
      	}
      	return returnVal;
      }
      function validateDates(){
          var returnval = false
          var startTime = $("#startTime").val();
          var completionTime = $("#completionTime").val();
          if(isValidDate(startTime) && isValidDate(completionTime)){
        	  returnval = true;
          }
          return returnval;
      }
    </script>
    </head>
    <body>
    <tds:subHeader title="Create Event" crumbs="['Planning','Event','Create' ]"/><br/>
        <div class="body">
            <!-- <h1>Create Event</h1> -->
            <div class="nav" style="border: 1px solid #CCCCCC; height: 25px">
		      <span class="menuButton"><g:link class="list" action="list">Events List</g:link></span>
		    </div>
		    <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b> </label>
                                </td>
                                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:moveEventInstance,field:'name')}"/>
                                    <g:hasErrors bean="${moveEventInstance}" field="name">
						            <div class="errors">
						                <g:renderErrors bean="${moveEventInstance}" as="list" field="name"/>
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:moveEventInstance,field:'description')}"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td class="name">
                                    <label for="moveBundle">Bundle:</label>
                                </td>
                                <td  style="text-align:left;" class="valueNW">
	                                <ul id="moveBundleList">
                                        <g:if test="${bundles.size() > 0}">
                                            <g:each in="${bundles}" var="bundle">
                                                <input type="checkbox" name="moveBundle" value="${bundle.id}"> &nbsp;${bundle.name}<br>
                                            </g:each>
                                        </g:if>
                                        <g:else>
                                            <li>There are no Bundles associated with the Project</li>
                                        </g:else>
	                                </ul>
                            	</td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="runbookStatus">Runbook Status:</label>
                                </td>
                                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'runbookStatus','errors')}">
                                    <g:select id="runbookStatus" name="runbookStatus" from="${moveEventInstance.constraints.runbookStatus.inList}" value="${moveEventInstance.runbookStatus}" ></g:select>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">Runbook bridge1 :</label>
                                </td>
                                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'runbookBridge1','errors')}">
                                    <input type="text" id="runbookBridge1" name="runbookBridge1" value="${fieldValue(bean:moveEventInstance,field:'runbookBridge1')}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">Runbook bridge2 :</label>
                                </td>
                                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'runbookBridge2','errors')}">
                                    <input type="text" id="runbookBridge2" name="runbookBridge2" value="${fieldValue(bean:moveEventInstance,field:'runbookBridge1')}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">Video Link:</label>
                                </td>
                                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'videolink','errors')}">
                                    <input type="text" id="videolink" name="videolink" value="${fieldValue(bean:moveEventInstance,field:'videolink')}"/>
                                </td>
                            </tr>
                        <tr class="prop">
				                <td class="name">
				                  <label for="newsBarMode"><b>News Bar Mode:&nbsp;<span style="color: red">*</span></b></label>
				                </td>
				                <td class="valueNW ${hasErrors(bean:moveEventInstance,field:'newsBarMode','errors')}">
				                  <g:select id="newsBarMode" name="newsBarMode" from="${moveEventInstance.constraints.newsBarMode.inList}" value="${moveEventInstance.newsBarMode}" valueMessagePrefix="event.newsBarMode"></g:select>
				                  <g:hasErrors bean="${moveEventInstance}" field="newsBarMode">
				                    <div class="errors">
				                      <g:renderErrors bean="${moveEventInstance}" as="list" field="newsBarMode"/>
				                    </div>
				                  </g:hasErrors>
				                </td>
			              </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">Estimated Start:</label>
                                </td>
				                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'estStartTime','errors')}">
				                  <script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#estStartTime").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat()  });
				                    });
				                  </script> <input type="text" class="dateRange" size="15" style="width: 210px;" id="estStartTime" name="estStartTime"
				                                   value="<tds:convertDateTime date="${moveEventInstance?.estStartTime}" format="12hrs" />" />
				                                   <g:hasErrors bean="${moveEventInstance}" field="estStartTime">
				                    <div class="errors">
				                      <g:renderErrors bean="${moveEventInstance}" as="list" field="estStartTime"/>
				                    </div>
				                  </g:hasErrors>
				                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
			<div class="buttons">
				<span class="button">
					<input class="save" type="submit" value="Save" />
				</span>
				<span class="button">
					<input type="button" class="cancel" value="Cancel" onclick="window.history.back()"/>
				</span>
			</div>
            </g:form>
        </div>
<script>
	currentMenuId = "#eventMenu";
    $(".menu-parent-planning-event-list").addClass('active');
    $(".menu-parent-planning").addClass('active');
</script>
    </body>
</html>
