/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Controller
class WelcomeController {

	private static final String EXAMPLE_COM = "https://example.com/";

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.connectTimeout(Duration.ofSeconds(30))
		.build();

	@GetMapping("/")
	public String welcome() throws IOException, InterruptedException {
		HttpResponse<String> response = HTTP_CLIENT.send(getRequest(), HttpResponse.BodyHandlers.ofString());

		return "welcome";
	}

	/**
	 * Obtain a HttpRequest object.
	 * @return HttpRequest
	 */
	private HttpRequest getRequest() {
		return HttpRequest.newBuilder()
			.GET()
			.uri(URI.create(EXAMPLE_COM))
			.setHeader("User-Agent", "Java 11 HttpClient Bot")
			.build();
	}

}
