package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * The valid options for the AssetComment domain property category.
 *
 * This should be an Enum but we need to first switch all of the references of a string to use the class reference
 * and then we can switch this to an Enum.
 */
@CompileStatic
class AssetCommentCategory {

	public static final String GENERAL   = 'general'
	public static final String DISCOVERY = 'discovery'
	public static final String PLANNING  = 'planning'
	public static final String WALKTHRU  = 'walkthru'
	public static final String PREMOVE   = 'premove'
	public static final String MOVEDAY   = 'moveday'
	public static final String SHUTDOWN  = 'shutdown'
	public static final String PHYSICAL  = 'physical'
	public static final String STARTUP   = 'startup'
	public static final String POSTMOVE  = 'postmove'
	public static final String VERIFY    = 'verify'
	public static final String ANALYSIS  = 'analysis'
	public static final String DESIGN    = 'design'
	public static final String BUILDOUT  = 'buildout'
	public static final String TRANSPORT = 'transport'
	public static final String CLOSEOUT  = 'closeout'
	public static final String LEARNING  = 'learning'

	static final List<String> list = [GENERAL, DISCOVERY, PLANNING, WALKTHRU, PREMOVE, MOVEDAY, SHUTDOWN,
	                                  PHYSICAL, STARTUP, POSTMOVE, VERIFY, ANALYSIS, DESIGN, BUILDOUT,
	                                  TRANSPORT, CLOSEOUT, LEARNING].asImmutable()

	static final List<String> preMoveCategories = [GENERAL, DISCOVERY, VERIFY, ANALYSIS, PLANNING, DESIGN,
	                                               BUILDOUT, WALKTHRU, PREMOVE, LEARNING].asImmutable()

	static final List<String> moveDayCategories = [MOVEDAY, SHUTDOWN, PHYSICAL, TRANSPORT, STARTUP].asImmutable()

	static final List<String> postMoveCategories = [POSTMOVE, CLOSEOUT, LEARNING].asImmutable()

	static final List<String> discoveryCategories = [DISCOVERY, VERIFY, LEARNING].asImmutable()

	static final List<String> planningCategories = [ANALYSIS, PLANNING, DESIGN, BUILDOUT].asImmutable()
}
