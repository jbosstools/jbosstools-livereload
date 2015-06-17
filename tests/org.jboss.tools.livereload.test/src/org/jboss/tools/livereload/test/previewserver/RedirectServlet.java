package org.jboss.tools.livereload.test.previewserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {

	private static final long serialVersionUID = -4460765872906375094L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getRequestURI().endsWith("/")) {
			response.setStatus(200);
		} else {
			response.setStatus(302);
			response.addHeader("Location", request.getRequestURL() + "/");
		}
	}
	
}