package capstone.server.gateway.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutes {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutes.class);

    @Bean
    public RouterFunction<ServerResponse> gatewayRouterFunctions(
            @Value("${services.auth-url:http://localhost:8090}") String authBaseUrl,
            @Value("${services.friends-url:http://localhost:8092}") String friendsBaseUrl) {
        return route("auth")
                .route(path("/api/auth/**"), http())
                .before(uri(URI.create(authBaseUrl)))
                .build()

                .and(route("messaging")
                        .route(path("/api/messaging/**"), http())
                        .before(uri(URI.create("http://localhost:8091")))
                        .build())

                .and(route("friends")
                        .route(path("/api/friends/**"), http())
                        .before(uri(URI.create(friendsBaseUrl)))
                        .build());
    }
}