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
   		<asset:image src="icons/comment_${ iconMode }.png" border="0px" />
	</a>

 	<input type="hidden" name="comment_${suffix}" id="comment_${suffix}" value="${dependency.comment}">

</g:if>
<g:else>
 	<g:if test="${ dependency.comment }" >
 		<a title="" data-toggle="popover" data-trigger="hover" data-content="${ dependency.comment }">
			<img id="comment_${dependency.id}" src="icons/comment.png" style="height: 19px" border="0px" />
		</a>
   	</g:if>
</g:else>

