package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 *
 * <p>This class represents, internally, comments command in an ETL script.</p>
 * <pre>
 *     read labels
 *     domain Device
 *     iterate {
 *     		extract 'name' load 'assetName' when populated
 *     		extract 'description' load 'comments'
 *     		load 'comments' with myCommentContentVar
 *     	}
 * </pre>
 * (see <a href="CommentElement#with">CommentElement with(Object value) </a>)
 * <br>
 * It is used to process comments adding them in {@code ETLProcessorResult}.
 * <pre>
 * "data": [
 *	{
 * 		"duplicate": false,
 * 		"errorCount": 0,
 * 		"errors": [],
 * 		"fields": { ... },
 * 		"comments": [
 * 			"comment #1",
 * 			"comment #2"
 * 		]
 * 		"ignore": false,
 * 		"op": "I",
 * 		"rowNum": 1,
 * 		"warn": false
 * 	},
 * 			...
 * ]
 * </pre>
 */
@CompileStatic
class CommentElement implements ETLCommand {

	/**
	 * ETLProcessor instance used for fields validations
	 */
	ETLProcessor processor
	/**
	 * ETLDomain instance used for validate every field added using whenFound and whenNotFound commands
	 */
	ETLDomain domain
	/**
	 * Comment long text
	 */
	String commentText

	/**
	 * Creates an instance of {@code CommentElement}
	 * @param processor
	 * @param domain
	 */
	CommentElement(ETLProcessor processor, ETLDomain domain) {
		this.processor = processor
		this.domain = domain
		if (!this.domain.isAsset()) {
			throw ETLProcessorException.invalidDomainForComments(this.domain)
		}
	}

	/**
	 * <p>Completes load 'comments' command.</p>
	 *
	 * <pre>
	 * 	load 'comments' with bigTextVar
	 * </pre>
	 * @param bigText
	 * @return
	 */
	CommentElement with(Object value) {
		this.commentText = ETLValueHelper.valueOf(value)
		processor.addComments(this)
		return this
	}
}
