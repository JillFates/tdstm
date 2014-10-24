<%-- 
 	Used to display the Asset Dependency Comment Icon(s) in the Dependency table and provides the comment dialog for edit
 	@param type - indicates type of dependency options (support|dependent)
	@param forWhom - when set to 'edit' it provides the edit/add icon plus the comment dialog
	@param dependency - the AssetDependency object
	@comment the _dependentAdd.gsp creates a dependency map instead of using real dependency object so if you add other property reference.
 --%>

<g:set var="iconMode" value="${ (dependency.comment?.size() ? 'edit' : 'add')}" />

<%-- Note that for the ADD action, there is a hidden TR which the id/name properties will have name_TAG so that the javascript can replace. Therefore
if type is blank, the extra underscore(_) will be avoided --%>
<g:set var="suffix" value="${ (type ? type+'_' : '') + dependency.id.toString()}"/>

<g:if test="${ forWhom == 'edit' }" >
	<a title="${ dependency.comment?.toString() }" 
	 	id="commLink_${suffix}" href="#" onclick="javascript:EntityCrud.openDepCommentDialog('${suffix}')">
   		<img id="comment_${dependency.id}" src="${resource(dir:'icons',file:'comment_' + iconMode + '.png')}" border="0px" />
	</a>

 	<input type="hidden" name="comment_${suffix}" id="comment_${suffix}" value="${dependency.comment}">

</g:if>
<g:else>
 	<g:if test="${ dependency.comment }" >
 		<%-- TODO : JPM 10/2014 : Change the show mode for comments to use some sort of bubble to support large text --%>
 		<a title="${ dependency.comment }"> 
   			<img id="comment_${dependency.id}" src="${resource(dir:'icons',file:'comment.png')}" border="0px" />
   		</a>
   		<%-- Trying to get bootstrap popover to work...
 		<a class="popover" data-toggle="popover" data-content="${dependency.comment}" data-placement="top" onclick="event.stopPropagation(); this.popover('toggle');"> 
   			<img id="comment_${dependency.id}" src="${resource(dir:'icons',file:'comment.png')}" border="0px" />
   		</a>
   		--%>
   	</g:if>
</g:else>

