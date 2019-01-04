<g:each in="${newsList}" var="news">
	<tr>
		<g:if test="${project=='All' }">
			<td width="auto">
				${news.moveEvent.project.name}
			</td>
		</g:if>
		<td width="150px">
			<tds:convertDateTime date="${news.dateCreated}" format="yyyy/mm-dd hh:mm a" />
		</td>
		<td><g:link action="index" params="[moveEvent:news?.moveEvent?.id]">
				${news.moveEvent.name}
			</g:link>
		</td>
		<td>
			${news.message}
		</td>
	</tr>
</g:each>
