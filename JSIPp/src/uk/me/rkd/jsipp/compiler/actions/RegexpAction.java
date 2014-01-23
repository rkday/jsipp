/**
 * 
 */
package uk.me.rkd.jsipp.compiler.actions;

/**
 * @author robertday
 *
 */
public class RegexpAction extends MessageAction {
	public static enum RegexpCheck {
		/**
		 * Regexp does not affect the success or failure of the call.
		 */
		IGNORE,
		/**
		 * If the regular expression does not match, the call is marked as failed.
		 */
		FAIL_ON_NO_MATCH,
		/**
		 * If the regular expression matches, the call is marked as failed.
		 */
		FAIL_ON_MATCH
	}
	/**
	 * @param regex - the regular expression to match against
	 * @param check - whether we should fail the call on a match, fail the call if not matched, or nether
	 * @param assignments - the names of the variables to assign match results to
	 * 
	 */
	public RegexpAction(String regex, RegexpCheck check, String[] assignments) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see uk.me.rkd.jsipp.MessageAction#perform(uk.me.rkd.jsipp.Message)
	 */
	@Override
	void perform(String message) {
		// TODO Auto-generated method stub

	}

}
