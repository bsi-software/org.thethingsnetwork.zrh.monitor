package org.thethingsnetwork.zrh.monitor.client.ui;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.AccNoiseField;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.CompleteTextField;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.DataField;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.GatewayEuiField;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.MaxNoiseField;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.PlainDataField;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageForm.MainBox.TopBox.TimestampField;

public class MessageForm extends AbstractForm {

	public MaxNoiseField getMaxNoiseField() {
		return getFieldByClass(MaxNoiseField.class);
	}

	public AccNoiseField getAccNoiseField() {
		return getFieldByClass(AccNoiseField.class);
	}

	public DataField getDataField() {
		return getFieldByClass(DataField.class);
	}

	public PlainDataField getPlainDataField() {
		return getFieldByClass(PlainDataField.class);
	}

	public CompleteTextField getCompleteTextField() {
		return getFieldByClass(CompleteTextField.class);
	}

	public TopBox getTopBox() {
		return getFieldByClass(TopBox.class);
	}

	public TimestampField getTimestampField() {
		return getFieldByClass(TimestampField.class);
	}

	public GatewayEuiField getGatewayEuiField() {
		return getFieldByClass(GatewayEuiField.class);
	}

	@Order(1000.0)
	public class MainBox extends AbstractGroupBox {


		@Order(0.0)
		public class TopBox extends AbstractGroupBox {

			@Order(1000.0)
			public class GatewayEuiField extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Eui");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}

				@Override
				protected int getConfiguredMaxLength() {
					return 128;
				}
			}

			@Order(2000.0)
			public class TimestampField extends AbstractDateTimeField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Timestamp");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}
			}

			@Order(3000.0)
			public class MaxNoiseField extends AbstractIntegerField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("MaxNoise");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}
			}

			@Order(4000.0)
			public class AccNoiseField extends AbstractIntegerField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("AccNoise");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}
			}

			@Order(5000.0)
			public class DataField extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Data");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}

				@Override
				protected int getConfiguredGridW() {
					return 2;
				}
			}

			@Order(6000.0)
			public class PlainDataField extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("PlainData");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}

				@Override
				protected int getConfiguredGridW() {
					return 2;
				}
			}

			@Order(7000.0)
			public class CompleteTextField extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("CompleteText");
				}

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}

				@Override
				protected boolean getConfiguredMultilineText() {
					return true;
				}

				@Override
				protected int getConfiguredGridH() {
					return 4;
				}

				@Override
				protected int getConfiguredGridW() {
					return 2;
				}
			}
		}

		@Order(2000)
		public class OkButton extends AbstractOkButton {
		}
	}

}
