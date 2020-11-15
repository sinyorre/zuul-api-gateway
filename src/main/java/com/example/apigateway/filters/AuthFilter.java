package com.example.apigateway.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

public class AuthFilter extends ZuulFilter {
    @Value("${authServer.url}")
    private String authServerUrl;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        System.out.println("Request Method : " + request.getMethod() + " Request URL : " + request.getRequestURL().toString());

        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearerToken);
        HttpEntity authRequest = new HttpEntity(headers);
        ResponseEntity<Boolean> response = new RestTemplate().exchange(authServerUrl, HttpMethod.POST, authRequest, Boolean.class);
        Boolean isValid = response.getBody();
        if(!isValid) {
            ctx.setSendZuulResponse(false); // Not forwarding to micro services
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            ctx.setResponseBody("UNAUTHORIZED");
            ctx.getResponse().setContentType("application/json");
        }
        return null;
    }
}
