/**
 * 
 */
package uk.me.rkd.jsipp.compiler.actions;

import java.util.zip.DataFormatException;

import org.w3c.dom.Node;

/**
 * @author robertday
 * 
 */
public abstract class MessageAction {
	abstract void perform(String message);

	/**
	 * @param xmlnode
	 *            - the XML <action> node representing this action
	 * @return A MessageAction subclass of the appropriate type, contructed based on the contents of the XML node
	 * @throws DataFormatException
	 *             if the action's name is unknown
	 */
	public static MessageAction fromActionNode(final Node xmlnode) throws DataFormatException {
		Node action = xmlnode.getFirstChild();
		if (action.getNodeName().equals("ereg")) {
			return null;
		}
		throw new DataFormatException(action.getNodeName() + " is an unknown action type");
	}
}
