package de.skit.grocy.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;

import java.util.List;

public class VueResourceResolver extends PathResourceResolver {

    private final Resource indexHtml;

    public VueResourceResolver(Resource indexHtml) {
        this.indexHtml = indexHtml;
    }

    @Override
    protected Resource resolveResourceInternal(
            @Nullable HttpServletRequest request,
            @NonNull String requestPath,
            @NonNull List<? extends Resource> locations,
            @NonNull ResourceResolverChain chain) {

        Resource resource = super.resolveResourceInternal(request, requestPath, locations, chain);
        if (isApiOrHealth(requestPath)) {
            return resource;
        }
        return resource != null ? resource : indexHtml;
    }

    private boolean isApiOrHealth(String requestPath) {
        return requestPath.equals("health")
                || requestPath.startsWith("health/")
                || requestPath.equals("api")
                || requestPath.startsWith("api/");
    }

}
