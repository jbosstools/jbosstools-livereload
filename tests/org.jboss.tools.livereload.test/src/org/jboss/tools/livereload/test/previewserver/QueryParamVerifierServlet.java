package org.jboss.tools.livereload.test.previewserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QueryParamVerifierServlet extends HttpServlet {

	private static final long serialVersionUID = 8502427396225099738L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getQueryString() != null && !request.getQueryString().isEmpty()) {
			response.getOutputStream().write("OK :-)".getBytes());
			response.setStatus(200);
		} else {
			response.getOutputStream().write("Expected query params :-(".getBytes());
			response.setStatus(400);
		}
	}
	
}