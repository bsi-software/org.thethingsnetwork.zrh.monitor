package org.thethingsnetwork.zrh.monitor.client;

import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractDoubleConfigProperty extends AbstractConfigProperty<Double> {
	
	@Override
	protected Double parse(String value) {
		if(StringUtility.hasText(value)) {
			return Double.parseDouble(value);
		}
		
		else {
			return getDefaultValue();
		}
	}

}
