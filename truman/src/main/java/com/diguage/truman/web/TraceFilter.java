package com.diguage.truman.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class TraceFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String queryString = httpRequest.getQueryString();
		String url = httpRequest.getRequestURL().toString() + (queryString == null ? "" : queryString);
//		String url = httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort()
//				+ httpRequest.getQueryString();

		System.out.println(url + " Start");
		chain.doFilter(request, response);
		System.out.println(url + " End");
	}

	@Override
	public void destroy() {

	}
}
