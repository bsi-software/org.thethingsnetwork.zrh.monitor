package org.thethingsnetwork.zrh.monitor.client.ui;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;

public class NoisemapPage extends HeatmapPage {
	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("NoiseMap");
	}

	@Override
	protected Class<? extends IForm> getConfiguredDetailForm() {
		return NoisemapForm.class;
	}

	@Override
	protected void execInitDetailForm() {
		((NoisemapForm)getDetailForm()).getCloseButton().setVisible(false);
	}

}
