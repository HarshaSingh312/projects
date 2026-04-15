package org.example;

import java.util.*;

enum AccessType {
    READ,
    WRITE,
    ADMIN
}

class ResourceAccess {
    private String resourceId;
    private List<String> accessList;

    public ResourceAccess(String resourceId, List<String> accessList) {
        this.resourceId = resourceId;
        this.accessList = accessList;
    }

    public String getResourceId() {
        return resourceId;
    }

    public List<String> getAccessList() {
        return accessList;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setAccessList(List<String> accessList) {
        this.accessList = accessList;
    }
}

class ResourceMapper {
    public static List<ResourceAccess> convertToReosurceAccessList(Map<String, Set<AccessType>> resourceList) {
        List<ResourceAccess> resourceAccesses = new ArrayList<>();
        for (String resourceId: resourceList.keySet()) {
            List<String> accessList = new ArrayList<>();
            for (AccessType accessType: resourceList.get(resourceId)) {
                accessList.add(accessType.name());
            }
            resourceAccesses.add(new ResourceAccess(resourceId, accessList));
        }
        return resourceAccesses;
    }
}

public class EmployeeAccessManagement {

    private Map<String, Map<String, Set<AccessType>>> employeeAccessMap = new HashMap<>();

    private AccessType safeParse(String accessType) {
        try {
            return AccessType.valueOf(accessType.toUpperCase());
        } catch (Exception e) {
            return null; // or throw controlled exception
        }
    }

    public void grantAccess(String employeeId, String resourceId, String accessType) {
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(accessType, "accessType must not be null");
        AccessType type = safeParse(accessType);
        if (Objects.isNull(type)) return;
        employeeAccessMap.computeIfAbsent(employeeId, k -> new HashMap<>())
                .computeIfAbsent(resourceId, k -> new HashSet<>())
                .add(type);
    }

    public void revokeAccess(String employeeId, String resourceId, String accessType) {
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");

        Map<String, Set<AccessType>> resourceMap = employeeAccessMap.get(employeeId);
        if (resourceMap == null) return;
        Set<AccessType> permissions = resourceMap.get(resourceId);
        if (permissions == null) return;
        if (accessType == null) {
            resourceMap.remove(resourceId);
        } else {
            AccessType type = safeParse(accessType);
            if (Objects.isNull(type)) return;
            permissions.remove(type);

            if (permissions.isEmpty()) {
                resourceMap.remove(resourceId);
            }
        }

        // cleanup employee if empty
        if (resourceMap.isEmpty()) {
            employeeAccessMap.remove(employeeId);
        }
    }

    public List<String> getAccess(String employeeId, String resourceId) {
        Map<String, Set<AccessType>> accessList = employeeAccessMap.get(employeeId);
        if (Objects.isNull(accessList)) return new ArrayList<>();
        Set<AccessType> permissions = accessList.get(resourceId);
        if (Objects.isNull(permissions)) return new ArrayList<>();
        if (permissions.isEmpty()) return new ArrayList<>();
        return permissions.stream().map(Enum::name).toList();
    }

    public List<ResourceAccess> getResources(String employeeId) {
        Map<String, Set<AccessType>> accessList = employeeAccessMap.get(employeeId);
        if (Objects.isNull(accessList)) return new ArrayList<>();
        return ResourceMapper.convertToReosurceAccessList(accessList);
    }
}
