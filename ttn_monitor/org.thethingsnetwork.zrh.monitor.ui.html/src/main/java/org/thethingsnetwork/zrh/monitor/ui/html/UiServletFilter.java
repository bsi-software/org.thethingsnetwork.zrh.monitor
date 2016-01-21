package org.thethingsnetwork.zrh.monitor.ui.html;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.authentication.AnonymousAccessController;
import org.eclipse.scout.rt.server.commons.authentication.ConfigFileCredentialVerifier;
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController.FormBasedAuthConfig;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController.TrivialAuthConfig;

/**
 * <h3>{@link UiServletFilter}</h3>
 * This is the main servlet filter used for the HTML UI.
 *
 * @author mzi
 */
public class UiServletFilter implements Filter {

  private final AnonymousAccessController m_anonymousAccessController = BEANS.get(AnonymousAccessController.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
	  m_anonymousAccessController.init();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;
    m_anonymousAccessController.handle(req, resp, chain);
  }

  @Override
  public void destroy() {
	  m_anonymousAccessController.destroy();
  }
}
