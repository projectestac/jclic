/*
 * TextActivityContentKit.java
 *
 * Created on 20 / octubre / 2006, 13:39
 */

package edu.xtec.jclic.automation;

import edu.xtec.jclic.activities.text.TextActivityDocument;

/** @author Francesc */
public class TextActivityContentKit {

  public TextActivityDocument tad;
  public String checkButtonText;
  public String prevScreenText;

  /** Creates a new instance of TextActivityContentKit */
  public TextActivityContentKit(
      TextActivityDocument tad, String checkButtonText, String prevScreenText) {
    this.tad = tad;
    this.checkButtonText = checkButtonText;
    this.prevScreenText = prevScreenText;
  }

  /**
   * Activities should implement this interface if based on {@link
   * edu.xtec.jclic.activities.text.TextActivityDocument} objects, in order to let {@link
   * edu.xtec.jclic.automation.AutoContentProvider} objects to check whether they are compatible
   * with them.
   */
  public interface Compatible {};
}
