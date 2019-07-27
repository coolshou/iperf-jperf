/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- ...
 */

package net.nlanr.jperf.ui;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class IntegerSpinner extends JSpinner
{
	private SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
	
	public IntegerSpinner(int min, int max, int step, int initialValue)
	{
		Integer iVal =  Integer.valueOf(initialValue);
		spinnerNumberModel.setValue(iVal);
		// spinnerNumberModel.setValue(new Integer(initialValue));
		Integer iStep =  Integer.valueOf(step);
		spinnerNumberModel.setStepSize(iStep);
		// spinnerNumberModel.setStepSize(new Integer(step));
		Integer iMax =  Integer.valueOf(max);
		spinnerNumberModel.setMaximum(iMax);
		// spinnerNumberModel.setMaximum(new Integer(max));
		Integer iMin =  Integer.valueOf(min);
		spinnerNumberModel.setMinimum(iMin);
		// spinnerNumberModel.setMinimum(new Integer(min));
		
		this.setModel(spinnerNumberModel);
		this.setPreferredSize(new Dimension(120, 20));
	}
	
	public IntegerSpinner(int min, int max, int initialValue)
	{
		this(min, max, 1, initialValue);
	}
	
	public Integer getValue()
	{
		return ((Number)super.getValue()).intValue();
	}
}