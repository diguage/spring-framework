package com.diguage.truman.web.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.springframework.web.servlet.DispatcherServlet;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.servlet;


public class UndertowContainer {
  public static final String WEBMVC = "/webmvc";

//  https://github.com/yarosla/spring-undertow/blob/master/src/main/java/ys/undertow/UndertowMain.java
//  https://www.baeldung.com/java-web-app-without-web-xml
//  TODO 未完成
  public static void main(String[] args) throws Throwable {
    DeploymentInfo servletBuilder = Servlets.deployment()
        .setClassLoader(UndertowContainer.class.getClassLoader())
        .setContextPath(WEBMVC)
        .setDeploymentName("webmvc.war")
        .addServlets(
            servlet("DispatcherServlet", DispatcherServlet.class)
                .addInitParam("message", "Hello D瓜哥")
                .addMapping("/*"));
    DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
    manager.deploy();

    HttpHandler servletHandler = manager.start();
    PathHandler path = Handlers.path(Handlers.redirect(WEBMVC))
        .addPrefixPath(WEBMVC, servletHandler);
    Undertow server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(path)
        .build();
    server.start();
  }
}
