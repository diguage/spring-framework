package com.diguage.truman.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * https://www.baeldung.com/java-web-app-without-web-xml
 *
 * @author D瓜哥, https://www.diguage.com
 */
@WebServlet(urlPatterns = "/about", name = "aboutServlet")
public class HomeServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		writer.println("This is the about page.");
	}
}
