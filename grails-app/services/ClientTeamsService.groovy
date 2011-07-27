

class ClientTeamsService {

    static transactional = true
	
	def appendCommentsToRemainderList( def params, def session ) {
		
		def truncatedComment
		if( ( params.enterNote ).length() > 25 ) {
			truncatedComment = params.enterNote.substring( 0, 25 )
		} else {
			truncatedComment = params.enterNote
		}
		def commentListFromSession = session.getAttribute ( "COMMENT_LIST" )
		if ( commentListFromSession ) {
			def commentList = commentListFromSession.split('~')
			def completeComment = session.getAttribute ( "COMMENT_COMPLETE" ).split('~')
			 switch ( commentList.length ) {
				 case 3:  session.setAttribute( "COMMENT_LIST", "${truncatedComment}~${commentList[0]}~${commentList[1]}~${commentList[2]}" )
						  session.setAttribute( "COMMENT_COMPLETE", "${params.enterNote}~${completeComment[0]}~${completeComment[1]}~${completeComment[2]}" )
						  break;
				case 4:  session.setAttribute( "COMMENT_LIST", "${truncatedComment}~${commentList[0]}~${commentList[1]}~${commentList[2]}~${commentList[3]}" )
						 session.setAttribute( "COMMENT_COMPLETE", "${params.enterNote}~${commentList[0]}~${completeComment[1]}~${completeComment[2]}~${completeComment[3]}" )
						 break;
				case 5:  session.setAttribute( "COMMENT_LIST", "${truncatedComment}~${commentList[0]}~${commentList[1]}~${commentList[2]}~${commentList[3]}~${commentList[4]}" )
						 session.setAttribute( "COMMENT_COMPLETE", "${params.enterNote}~${commentList[0]}~${commentList[1]}~${completeComment[2]}~${completeComment[3]}~${completeComment[4]}" )
						 break;
				case 6:  session.setAttribute( "COMMENT_LIST", "${truncatedComment}~${commentList[0]}~${commentList[1]}~${commentList[3]}~${commentList[4]}~${commentList[5]}" )
						 session.setAttribute( "COMMENT_COMPLETE", "${params.enterNote}~${commentList[0]}~${commentList[1]}~${completeComment[3]}~${completeComment[4]}~${completeComment[5]}" )
						 break;
			 }
		}
	}
		
	def getCommentsFromRemainderList( def session ) {
		def commentsList
		def commentListFromSession = session.getAttribute ( "COMMENT_LIST" )
		if ( commentListFromSession ) {
			commentsList = commentListFromSession.split('~')
			switch ( commentsList.length ) {
				case 3:  commentsList = [ commentsList[0], commentsList[1], commentsList[2] ]
						 break;
				case 4:  commentsList = [ commentsList[0], commentsList[1], commentsList[2], commentsList[3] ]
						 break;
				case 5:  commentsList = [ commentsList[0], commentsList[1], commentsList[2], commentsList[3], commentsList[4] ]
						 break;
				case 6:  commentsList = [ commentsList[0], commentsList[1], commentsList[2], commentsList[3], commentsList[4], commentsList[5] ]
						 break;
			}
		} else {
			commentsList = ["Device not powered down", "Device is not in expected rack", "Device will not power up" ]
			session.setAttribute ( "COMMENT_LIST", "Device not powered down~Device is not in expected rack~Device will not power up" )
			session.setAttribute ( "COMMENT_COMPLETE", "Device not powered down~Device is not in expected rack~Device will not power up" )
		}
		return commentsList
	}
}
