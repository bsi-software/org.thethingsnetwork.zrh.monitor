package org.thethingsnetwork.zrh.monitor.client.ui;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceForm.MainBox.TopBox;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceForm.MainBox.TopBox.LatitudeField;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceForm.MainBox.TopBox.LongitudeField;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceForm.MainBox.TopBox.NameField;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceForm.MainBox.TopBox.NoiseField;

public class DeviceForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("AdditionalInformation");
	}
	
	public LatitudeField getLatitudeField() {
		return getFieldByClass(LatitudeField.class);
	}

	public LongitudeField getLongitudeField() {
		return getFieldByClass(LongitudeField.class);
	}

	public NoiseField getNoiseField() {
		return getFieldByClass(NoiseField.class);
	}

	public NameField getNameField() {
		return getFieldByClass(NameField.class);
	}

	public TopBox getTopBox() {
		return getFieldByClass(TopBox.class);
	}

	@Order(1000.0)
	public class MainBox extends AbstractGroupBox {

		@Order(1000.0)
		public class TopBox extends AbstractGroupBox {

			@Order(1000.0)
			public class NameField extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Name");
				}

				@Override
				protected int getConfiguredMaxLength() {
					return 128;
				}
			}


			@Order(1500.0)
			public class NoiseField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("NoiseNode");
				}
			}
			
			@Order(2000.0)
			public class EmptyField extends AbstractPlaceholderField {
			}

			@Order(3000.0)
			public class LatitudeField extends AbstractBigDecimalField {	
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Latitude");
				}

				@Override
				protected BigDecimal getConfiguredMinValue() {
					return new BigDecimal("-90.0");
				}

				@Override
				protected BigDecimal getConfiguredMaxValue() {
					return new BigDecimal("90.0");
				}
			}

			@Order(4000.0)
			public class LongitudeField extends AbstractBigDecimalField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Longitude");
				}

				@Override
				protected BigDecimal getConfiguredMinValue() {
					return new BigDecimal("-180.0");
				}

				@Override
				protected BigDecimal getConfiguredMaxValue() {
					return new BigDecimal("180.0");
				}
			}
		}

		@Order(2000)
		public class OkButton extends AbstractOkButton {
		}

		@Order(3000)
		public class CancelButton extends AbstractCancelButton {
		}
	}
}
