package com.cartoonizer.rag.guardrails;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public class SecurityPolicy {

    private final List<Path> allowedFolders;
    private final List<String> allowedRemoteHosts;
    private final List<String> blacklistedRemoteHosts;

    public SecurityPolicy(List<Path> allowedFolders, List<String> allowedRemoteHosts, List<String> blacklistedRemoteHosts) {
        this.allowedFolders = allowedFolders;
        this.allowedRemoteHosts = allowedRemoteHosts;
        this.blacklistedRemoteHosts = blacklistedRemoteHosts;
    }

    public boolean isAllowedLocalPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        for (Path allowedFolder : allowedFolders) {
            Path allowed = allowedFolder.toAbsolutePath().normalize();
            if (normalized.startsWith(allowed)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowedRemoteUri(URI uri) {
        if (uri == null || uri.getHost() == null) {
            return false;
        }
        // Only allow https requests
        String host = uri.getHost().toLowerCase();
        String path = uri.getPath();
        String hostWithPath = host + "/" + path;
        if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
            return false;
        }

        // Block authenticated/private endpoints by policy:
        // - only allow known hosts
        // - no credentials in URI
        // - no query-based auth tokens
        if (uri.getUserInfo() != null) {
            return false;
        }

        for (String blacklisted : blacklistedRemoteHosts) {
            if (host.equalsIgnoreCase(blacklisted) || 
                    hostWithPath.toLowerCase().contains(blacklisted.toLowerCase())) {
                return false;
            }
        }
        for (String allowedHost : allowedRemoteHosts) {
            if (host.equalsIgnoreCase(allowedHost) || host.endsWith("." + allowedHost.toLowerCase()) ||
                    hostWithPath.toLowerCase().contains(allowedHost.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public List<Path> allowedFolders() {
        return allowedFolders;
    }

    public List<String> allowedRemoteHosts() {
        return allowedRemoteHosts;
    }

    List<String> blacklistedRemoteHosts() {
        return blacklistedRemoteHosts;
    }
}
