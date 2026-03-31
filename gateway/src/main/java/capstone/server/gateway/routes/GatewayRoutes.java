package capstone.server.gateway.routes;

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

    @Bean
    public RouterFunction<ServerResponse> gatewayRouterFunctions() {
        return route("auth")
                .route(path("/api/auth/**"), http())
                .before(uri(URI.create("http://localhost:8090")))
                .build()
                .and(route("messaging_rest") // REST
                        .route(path("/messages/**"), http())
                        .before(uri(URI.create("http://localhost:8091")))
                        .build())
                .and(route("messaging_ws") // WebSocket
                        .route(path("/ws/**"), http())
                        .before(uri(URI.create("http://localhost:8091")))
                        .build())
                .and(route("friends")
                        .route(path("/api/friends/**"), http())
                        .before(uri(URI.create("http://localhost:8092")))
                        .build());
    }
}