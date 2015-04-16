/**
 * Copyright (C) 2014 Federico Recio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.federecio.dropwizard.swagger;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import io.dropwizard.Configuration;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Federico Recio
 * @author Flemming Frandsen
 */
public class SwaggerConfiguration {

    private final Configuration configuration;
    private final String staticAssetPrefix;

    public SwaggerConfiguration(Configuration configuration, String staticAssetPrefix) {
        this.staticAssetPrefix = staticAssetPrefix;
        this.configuration = configuration;
    }

    public void setUpSwaggerFor(String host, Integer port) {
        SwaggerConfig config = ConfigFactory.config();
        String swaggerBasePath = getSwaggerBasePath(host, port);
        config.setBasePath(swaggerBasePath);
        config.setApiPath(swaggerBasePath);
        ConfigFactory.setConfig(config);
    }

    public String getJerseyRootPath() {
        String rootPath;

        ServerFactory serverFactory = configuration.getServerFactory();

        if (serverFactory instanceof SimpleServerFactory) {
            rootPath = ((SimpleServerFactory) serverFactory).getJerseyRootPath();
        } else {
            rootPath = ((DefaultServerFactory) serverFactory).getJerseyRootPath();
        }

        return stripUrlSlashes(rootPath);
    }

    public String getApplicationContextPath() {
         String applicationContextPath;

        ServerFactory serverFactory = configuration.getServerFactory();

        if (serverFactory instanceof SimpleServerFactory) {
            applicationContextPath = ((SimpleServerFactory) serverFactory).getApplicationContextPath();
        } else {
            applicationContextPath = ((DefaultServerFactory) serverFactory).getApplicationContextPath();
        }

        return stripUrlSlashes(applicationContextPath);
    }

    public String getUrlPattern() {
        final String applicationContextPath = getApplicationContextPath();
        final String rootPath = getJerseyRootPath();

        String urlPattern;

        if (rootPath.equals("/") && applicationContextPath.equals("/") &&
            (StringUtils.isEmpty(staticAssetPrefix) || staticAssetPrefix.equals("/"))) {
            urlPattern =  "/";
        } else if (rootPath.equals("/") && !applicationContextPath.equals("/")) {
            urlPattern = applicationContextPath;
        } else if (!rootPath.equals("/") && applicationContextPath.equals("/")) {
            urlPattern = rootPath;
        } else if (!StringUtils.isEmpty(staticAssetPrefix) && !staticAssetPrefix.equals("/")) {
            urlPattern = staticAssetPrefix;
        } else {
            urlPattern = applicationContextPath + rootPath;
        }

        return urlPattern;
    }

    private String stripUrlSlashes(String urlToStrip) {
        if (urlToStrip.endsWith("/*")) {
            urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
        }

        if (urlToStrip.length() > 1 && urlToStrip.endsWith("/")) {
            urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
        }

        return urlToStrip;
    }

    private String getSwaggerBasePath(String host, Integer port) {
        HttpConnectorFactory httpConnectorFactory = getHttpConnectionFactory();

        if (httpConnectorFactory == null) {
            throw new IllegalStateException("Could not get HttpConnectorFactory");
        }

        String protocol = httpConnectorFactory instanceof HttpsConnectorFactory ? "https" : "http";
        String urlPattern = getUrlPattern();
        if (port == null) {
            port = httpConnectorFactory.getPort();
        }
        if (!"/".equals(urlPattern)) {
            return String.format("%s://%s:%s%s", protocol, host, port, urlPattern);
        } else {
            return String.format("%s://%s:%s", protocol, host, port);
        }
    }

    private HttpConnectorFactory getHttpConnectionFactory() {
        List<ConnectorFactory> connectorFactories = getConnectorFactories();
        for (ConnectorFactory connectorFactory : connectorFactories) {
            if (connectorFactory instanceof HttpsConnectorFactory) {
                return (HttpConnectorFactory) connectorFactory;  // if we find https skip the others
            }
        }
        for (ConnectorFactory connectorFactory : connectorFactories) {
            if (connectorFactory instanceof HttpConnectorFactory) {
                return (HttpConnectorFactory) connectorFactory; // if not https pick http
            }
        }

        throw new IllegalStateException("Unable to find an HttpServerFactory");
    }

    private List<ConnectorFactory> getConnectorFactories() {
        ServerFactory serverFactory = configuration.getServerFactory();
        if (serverFactory instanceof SimpleServerFactory) {
            return Collections.singletonList(((SimpleServerFactory) serverFactory).getConnector());
        } else if (serverFactory instanceof DefaultServerFactory) {
            return new ArrayList<>(((DefaultServerFactory) serverFactory).getApplicationConnectors());
        } else {
            throw new IllegalStateException("Unknown ServerFactory implementation: " + serverFactory.getClass());
        }
    }
}
