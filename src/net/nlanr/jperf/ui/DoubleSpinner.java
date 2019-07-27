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

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class DoubleSpinner extends JSpinner
{
	private SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
	
	public DoubleSpinner(double min, double max, double step, double initialValue)
	{
		Double dVal = Double.valueOf(initialValue);
		spinnerNumberModel.setValue(dVal);
		// spinnerNumberModel.setValue(new Double(initialValue));
		Double dStep = Double.valueOf(step);
		spinnerNumberModel.setStepSize(dStep);
		// spinnerNumberModel.setStepSize(new Double(step));
		Double dMax = Double.valueOf(max);
		spinnerNumberModel.setMaximum(dMax);
		// spinnerNumberModel.setMaximum(new Double(max));
		Double dMin = Double.valueOf(min);
		spinnerNumberModel.setMinimum(dMin);
		// spinnerNumberModel.setMinimum(new Double(min));
		
		this.setModel(spinnerNumberModel);
	}
	
	public DoubleSpinner(double min, double max, double initialValue)
	{
		this(min, max, 1.0, initialValue);
	}
	
	public Double getValue()
	{
		Object val = super.getValue();
		if (val instanceof Number)
		{
			return ((Number)val).doubleValue();
		}
		else
		{
			return ((Number)spinnerNumberModel.getMinimum()).doubleValue();
		}
	}
}
