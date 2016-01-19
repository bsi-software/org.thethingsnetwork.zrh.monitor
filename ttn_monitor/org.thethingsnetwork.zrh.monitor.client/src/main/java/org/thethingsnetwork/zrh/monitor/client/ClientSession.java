package org.thethingsnetwork.zrh.monitor.client;

import java.util.Locale;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop.DesktopStyle;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.shared.SharedConfigProperties.CreateTunnelToServerBeansProperty;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thethingsnetwork.zrh.monitor.client.ui.Desktop;

/**
 * <h3>{@link ClientSession}</h3>
 *
 * @author mzi
 */
public class ClientSession extends AbstractClientSession {
	  private static Logger LOG = LoggerFactory.getLogger(ClientSession.class);

  public ClientSession() {
    super(true);
  }

  /**
   * @return The {@link IClientSession} which is associated with the current thread, or <code>null</code> if not found.
   */
  public static ClientSession get() {
    return ClientSessionProvider.currentSession(ClientSession.class);
  }

  @Override
  protected void execLoadSession() {
	    Boolean createTunnelToServerBeans = CONFIG.getPropertyValue(CreateTunnelToServerBeansProperty.class);
	    createTunnelToServerBeans = false;
	    if (!createTunnelToServerBeans) {
	      LOG.info("starting client without a server");
	    }

	    execInitLocale();
	    CODES.getAllCodeTypes("org.eclipsescout.demo.widgets.shared");
	    setDesktop(new Desktop(resolveDesktopStyle()));

	    if (createTunnelToServerBeans) {
	      BEANS.get(IBookmarkService.class).loadBookmarks();
	      BEANS.get(IPingService.class).ping("ping");
	    }
  }
  
  /**
   * Returns the 'desktopStyle' provided as part of the URL, or the default style otherwise.<br/>
   * E.g. http://[host:port]/?desktopStyle=BENCH to start in bench mode.
   */
  protected DesktopStyle resolveDesktopStyle() {
    String desktopStyle = PropertyMap.CURRENT.get().get("desktopStyle");
    if (desktopStyle != null) {
      return DesktopStyle.valueOf(desktopStyle);
    }
    else {
      return DesktopStyle.DEFAULT;
    }
  }

  /**
   * Sets the session locale <i>before</i> the desktop is created. The default implementation sets the locale to
   * {@link Locale#ENGLISH} to get a consistent state across the entire widget application (for most languages, not all
   * tests are localized, except for English). Subclasses may override this method.
   */
  protected void execInitLocale() {
    setLocale(Locale.ENGLISH);
  }
}
