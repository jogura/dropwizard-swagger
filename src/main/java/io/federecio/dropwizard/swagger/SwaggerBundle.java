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

import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author Tristan Burch
 */
public class SwaggerBundle<T extends Configuration> extends SwaggerDropwizard<T> {

    @Override
    public void run(T configuration, Environment environment) {
        SwaggerBundleConfiguration bundleConfiguration = getSwaggerBundleConfiguration(configuration);
        try {
            if (bundleConfiguration == null) {
                onRun(configuration, environment);
            } else {
                SwaggerConfiguration swaggerConfiguration =
                    SwaggerConfiguration
                        .builder()
                        .configuration(configuration)
                        .staticAssetPrefix(bundleConfiguration.getStaticAssetPrefix())
                    .tokenType(bundleConfiguration.getSecurityTokenType())
                    .authHeader(bundleConfiguration.getAuthHeader())
                    .build();

                String host = StringUtils.isEmpty(bundleConfiguration.getHost()) ? SwaggerHostResolver.getSwaggerHost() : bundleConfiguration.getHost();
                onRun(configuration, environment, host, bundleConfiguration.getPort(), swaggerConfiguration);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Override this method to provide your own {@link SwaggerBundleConfiguration} which can be constructed
     * using Dropwizard's configuration instance passed as parameter
     */
    @SuppressWarnings("unused")
    public SwaggerBundleConfiguration getSwaggerBundleConfiguration(T configuration) {
        try {
            return new SwaggerBundleConfiguration(SwaggerHostResolver.getSwaggerHost());
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't determine host");
        }
    }
}
