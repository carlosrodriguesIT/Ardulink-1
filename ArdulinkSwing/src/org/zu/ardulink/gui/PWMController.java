/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.gui;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.event.PWMChangeEvent;
import org.zu.ardulink.gui.event.PWMControllerListener;
import org.zu.ardulink.gui.facility.IntMinMaxModel;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class can manage power with modulation arduino pins sending specific messages to
 * the arduino board. It has many components to ensure maximum flexibility in the
 * management of these pins.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class PWMController extends JPanel implements Linkable {

	private static final long serialVersionUID = 7927439571760351922L;
	
	private JSlider powerSlider;
	private JComboBox valueComboBox;
	private IntMinMaxModel valueComboBoxModel;
	private JLabel voltValueLbl;
	private JCheckBox chckbxContChange;
	private JProgressBar progressBar;
	private IntMinMaxModel maxValueComboBoxModel;
	private IntMinMaxModel minValueComboBoxModel;
	private JComboBox pinComboBox;
	private JLabel lblPowerPinController;
	
	private List<PWMControllerListener> pwmControllerListeners = new LinkedList<PWMControllerListener>();
	
	private Link link = Link.getDefaultInstance();

	/**
	 * Create the panel.
	 */
	public PWMController() {
		setPreferredSize(new Dimension(195, 260));
		setLayout(null);
		
		powerSlider = new JSlider();
		powerSlider.setFont(new Font("SansSerif", Font.PLAIN, 11));
		powerSlider.setMajorTickSpacing(15);
		powerSlider.setPaintLabels(true);
		powerSlider.setPaintTicks(true);
		powerSlider.setMaximum(255);
		powerSlider.setValue(0);
		powerSlider.setOrientation(SwingConstants.VERTICAL);
		powerSlider.setBounds(126, 38, 59, 199);
		add(powerSlider);
		
		JLabel lblPowerPin = new JLabel("Power Pin:");
		lblPowerPin.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblPowerPin.setBounds(10, 40, 59, 14);
		add(lblPowerPin);
		
		// TODO definire un metodo per poter cambiare l'insieme dei pin controllabili. In questo modo si pu� lavorare anche con schede diverse da Arduino UNO
		// pinComboBox.setModel(new DefaultComboBoxModel(new String[] {"3", "5", "6", "9", "10", "11"}));
		IntMinMaxModel pinComboBoxModel = new IntMinMaxModel(0, 40);
		pinComboBox = new JComboBox(pinComboBoxModel);
		pinComboBox.setSelectedItem(Integer.valueOf(11));
		pinComboBox.setBounds(65, 36, 55, 22);
		add(pinComboBox);
		
		maxValueComboBoxModel = new IntMinMaxModel(0, 255).withLastItemSelected();
		JComboBox maxValueComboBox = new JComboBox(maxValueComboBoxModel);
		maxValueComboBox.setBounds(65, 65, 55, 22);
		add(maxValueComboBox);

		minValueComboBoxModel = new IntMinMaxModel(0, 255).withFirstItemSelected();
		JComboBox minValueComboBox = new JComboBox(minValueComboBoxModel);
		minValueComboBox.setBounds(65, 217, 55, 22);
		add(minValueComboBox);
		
		JLabel lblMaxValue = new JLabel("Max Value:");
		lblMaxValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblMaxValue.setBounds(10, 69, 59, 14);
		add(lblMaxValue);
		
		JLabel lblMinValue = new JLabel("Min Value:");
		lblMinValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblMinValue.setBounds(10, 221, 59, 14);
		add(lblMinValue);
		
		progressBar = new JProgressBar();
		progressBar.setFont(new Font("SansSerif", Font.PLAIN, 11));
		progressBar.setStringPainted(true);
		progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		progressBar.setOrientation(SwingConstants.VERTICAL);
		progressBar.setBounds(96, 98, 16, 108);
		add(progressBar);
		
		lblPowerPinController = new JLabel("Power Pin Controller");
		lblPowerPinController.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lblPowerPinController.setToolTipText("Power With Modulation");
		lblPowerPinController.setHorizontalAlignment(SwingConstants.CENTER);
		lblPowerPinController.setBounds(10, 11, 175, 14);
		add(lblPowerPinController);
		
		JLabel lblVoltOutput = new JLabel("Volt Output:");
		lblVoltOutput.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblVoltOutput.setBounds(10, 143, 59, 14);
		add(lblVoltOutput);
		
		voltValueLbl = new JLabel("0V");
		voltValueLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
		voltValueLbl.setBounds(10, 157, 76, 14);
		add(voltValueLbl);
		
		JLabel lblCurrentValue = new JLabel("Current Value:");
		lblCurrentValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblCurrentValue.setBounds(10, 98, 76, 14);
		add(lblCurrentValue);
		
		valueComboBoxModel = new IntMinMaxModel(0, 255).withFirstItemSelected();
		valueComboBox = new JComboBox(valueComboBoxModel);
		valueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int comboBoxCurrentValue = valueComboBoxModel.getSelectedItem().intValue();
				int powerSliderCurrentValue = powerSlider.getValue();
				if(comboBoxCurrentValue != powerSliderCurrentValue) {
					powerSlider.setValue(comboBoxCurrentValue);
				}
			}
		});
		valueComboBox.setBounds(10, 112, 55, 22);
		add(valueComboBox);
		
		JLabel lblContinuousChange = new JLabel("Cont. Change:");
		lblContinuousChange.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblContinuousChange.setToolTipText("Continuous Change");
		lblContinuousChange.setBounds(10, 176, 73, 14);
		add(lblContinuousChange);
		
		chckbxContChange = new JCheckBox("");
		chckbxContChange.setRequestFocusEnabled(false);
		chckbxContChange.setRolloverEnabled(true);
		chckbxContChange.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		chckbxContChange.setSelected(true);
		chckbxContChange.setBounds(6, 188, 21, 22);
		add(chckbxContChange);
		
		powerSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!powerSlider.getValueIsAdjusting() || chckbxContChange.isSelected()) {
			        int powerValue = (int)powerSlider.getValue();
			        valueComboBoxModel.setSelectedItem(powerValue);
			        float volt = ((float)(((float)powerValue)*5.0f))/255.0f;
					voltValueLbl.setText(String.valueOf(volt) + "V");
			        float progress  = ((float)(((float)(powerValue - powerSlider.getMinimum()))*100.0f))/((float)powerSlider.getMaximum() - (float)powerSlider.getMinimum());
			        progressBar.setValue((int)progress);
			        
			        notifyListeners(powerValue);
			        
			        int pin = ((Integer)pinComboBox.getSelectedItem()).intValue();
			        link.sendPowerPinIntensity(pin, powerValue);
			    }
			}

		});
		
		minValueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int maximum = maxValueComboBoxModel.getSelectedItem().intValue();
				int minimum = minValueComboBoxModel.getSelectedItem().intValue();
				
				if(minimum > maximum) {
					minValueComboBoxModel.setSelectedItem(maximum);
				}
				
				valueComboBoxModel = new IntMinMaxModel(minimum, maximum);
				valueComboBox.setModel(valueComboBoxModel);
				powerSlider.setMinimum(minimum);
			}
		});

		maxValueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int maximum = maxValueComboBoxModel.getSelectedItem().intValue();
				int minimum = minValueComboBoxModel.getSelectedItem().intValue();

				if(minimum > maximum) {
					maxValueComboBoxModel.setSelectedItem(minimum);
				}
				
				valueComboBoxModel = new IntMinMaxModel(minimum, maximum);
				valueComboBox.setModel(valueComboBoxModel);
				powerSlider.setMaximum(maximum);
			}
		});

	}

	/**
	 * Set the pin to control
	 * @param pin
	 */
	public void setPin(int pin) {
		pinComboBox.setSelectedItem(Integer.valueOf(pin));
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public ReplyMessageCallback getReplyMessageCallback() {
		throw new RuntimeException("Not developed yet");
	}

	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		throw new RuntimeException("Not developed yet");
	}
	
	public void setTitle(String title) {
		lblPowerPinController.setText(title);
	}
	
	public boolean addPWMControllerListener(PWMControllerListener listener) {
		return pwmControllerListeners.add(listener);
	}
	
	public boolean removePWMControllerListener(PWMControllerListener listener) {
		return pwmControllerListeners.remove(listener);
	}

	private void notifyListeners(int powerValue) {
		PWMChangeEvent event = new PWMChangeEvent(this, powerValue);
		for (PWMControllerListener pwmControllerListener : pwmControllerListeners) {
			pwmControllerListener.pwmChanged(event);
		}
	}
	
	public int getValue() {
		return valueComboBoxModel.getSelectedItem().intValue();
	}

	public void setValue(int value) {
		int maximum = maxValueComboBoxModel.getSelectedItem().intValue();
		int minimum = minValueComboBoxModel.getSelectedItem().intValue();
		valueComboBoxModel.setSelectedItem(max(min(value, maximum), minimum));
	}

}
