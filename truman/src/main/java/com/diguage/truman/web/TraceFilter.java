package com.diguage.truman.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author D瓜哥, https://www.diguage.com
 */
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
