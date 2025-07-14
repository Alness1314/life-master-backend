package com.alness.lifemaster.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	private static final String SCHEME_NAME = "JWT Auth";
	private static final String SCHEME = "Bearer";
	private static final String BEARER_FORMAT = "JWT";

	@Value("${swg.server.prod}")
    private String serverProd;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(getInfo())
				.addServersItem(new Server().url("http://localhost:7091").description("Development Server"))
				.addServersItem(new Server().url(serverProd).description("Prod Server"))
				.components(createComponents())
				.addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
	}

	private Info getInfo() {
		return new Info()
				.title("LifeMaster")
				.description(
						"Application to manage income, expenses, attendance, diary, passwords, notes and generate monthly reports. Available on mobile and web.")
				.version("1.0")
				.license(new License()
						.name("Alness Zadro")
						.url("https://github.com/Alness1314"));
	}

	private Components createComponents() {
		return new Components().addSecuritySchemes(SCHEME_NAME, createSecurityScheme());
	}

	private SecurityScheme createSecurityScheme() {
		return new SecurityScheme()
				.name(SCHEME_NAME)
				.type(SecurityScheme.Type.HTTP)
				.scheme(SCHEME)
				.bearerFormat(BEARER_FORMAT)
				.description("Enter your JWT token in the format: Bearer {token}");
	}
}
