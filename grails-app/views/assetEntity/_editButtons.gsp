<g:if test="${redirectTo!='dependencyConsole'}">
	 <g:if test="${redirectTo=='listTask'}">
	 	<span class="button"><input type="button" class="save updateDep" value="Update/Close" onclick="updateToRefresh()" /></span>
	 </g:if>
	 <g:else>
	 	<span class="button"><g:actionSubmit class="save updateDep" value="Update/Close" action="Update" /></span>
	 </g:else>
	 <span class="button"><input type="button" class="save updateDep" value="Update/View" onclick="updateToShow('${whom}'); " /> </span>
	 <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
	 <span class="button"><input type="button" class="delete" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:if>
<g:else>
	 <span class="button"><input id="updatedId" name="updatedId" type="button" class="save updateDep" value="Update/Close" onclick="submitRemoteForm()"> </span>
	 <span class="button"><input type="button" class="save updateDep" value="Update/View" onclick="updateToShow('${whom}')" /> </span>
	 <span class="button"><input type="button" id="deleteId" name="deleteId"  class="save" value="Delete" onclick=" deleteAsset('${value}','${whom}')" /> </span>
	 <span class="button"><input type="button" class="delete" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:else>