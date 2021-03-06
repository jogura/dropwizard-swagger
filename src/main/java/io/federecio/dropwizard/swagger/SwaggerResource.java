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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Constants.SWAGGER_PATH)
@Produces(MediaType.TEXT_HTML)
public class SwaggerResource {
    private final String urlPattern;
    private final String tokenType;
    private final String authHeader;

    public SwaggerResource(String urlPattern, String tokenType, String authHeader) {
        this.urlPattern = urlPattern;
        this.tokenType = tokenType;
        this.authHeader = authHeader;
    }

    @GET
    public SwaggerView get() {
        return new SwaggerView(urlPattern, tokenType, authHeader);
    }
}
