package com.cms.permissions.aspect;

import com.cms.permissions.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class PermissionAuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAuditAspect.class);

    @Autowired
    private AuditService auditService;

    // Pointcut for permission operations in PermissionService
    private static final String PERMISSION_SERVICE_OPERATIONS =
        "execution(* com.cms.permissions.service.PermissionService.*(..))";

    // Pointcut for role operations in RoleService
    private static final String ROLE_SERVICE_OPERATIONS =
        "execution(* com.cms.permissions.service.RoleService.*(..))";

    // Pointcut for user operations that affect permissions in UserService
    private static final String USER_PERMISSION_OPERATIONS =
        "execution(* com.cms.permissions.service.UserService.assignRole*(..)) || " +
        "execution(* com.cms.permissions.service.UserService.removeRole*(..)) || " +
        "execution(* com.cms.permissions.service.UserService.updateUser*(..))";

    // Pointcut for document operations that affect permissions in DocumentService
    private static final String DOCUMENT_PERMISSION_OPERATIONS =
        "execution(* com.cms.permissions.service.DocumentService.updatePermissions*(..)) || " +
        "execution(* com.cms.permissions.service.DocumentService.assignPermission*(..)) || " +
        "execution(* com.cms.permissions.service.DocumentService.removePermission*(..))";

    /**
     * Log successful permission operations
     */
    @AfterReturning(pointcut = PERMISSION_SERVICE_OPERATIONS, returning = "result")
    public void logPermissionServiceOperation(JoinPoint joinPoint, Object result) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "PERMISSION";
            String resourceId = getResourceId(joinPoint, result);
            String resourceName = getResourceName(joinPoint, result);
            String[] targets = getTargetInfo(joinPoint);

            String details = buildOperationDetails(joinPoint, result);

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                resourceName,
                targets[0], // targetUser
                targets[1], // targetRole
                targets[2], // targetPermission
                "SUCCESS",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging permission operation", e);
        }
    }

    /**
     * Log failed permission operations
     */
    @AfterThrowing(pointcut = PERMISSION_SERVICE_OPERATIONS, throwing = "exception")
    public void logPermissionServiceOperationFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "PERMISSION";
            String resourceId = getResourceId(joinPoint, null);

            String details = "Operation failed: " + exception.getMessage();

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                "FAILURE",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging permission operation failure", e);
        }
    }

    /**
     * Log successful role operations
     */
    @AfterReturning(pointcut = ROLE_SERVICE_OPERATIONS, returning = "result")
    public void logRoleServiceOperation(JoinPoint joinPoint, Object result) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "ROLE";
            String resourceId = getResourceId(joinPoint, result);
            String resourceName = getResourceName(joinPoint, result);
            String[] targets = getTargetInfo(joinPoint);

            String details = buildOperationDetails(joinPoint, result);

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                resourceName,
                targets[0], // targetUser
                targets[1], // targetRole
                targets[2], // targetPermission
                "SUCCESS",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging role operation", e);
        }
    }

    /**
     * Log failed role operations
     */
    @AfterThrowing(pointcut = ROLE_SERVICE_OPERATIONS, throwing = "exception")
    public void logRoleServiceOperationFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "ROLE";
            String resourceId = getResourceId(joinPoint, null);

            String details = "Operation failed: " + exception.getMessage();

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                "FAILURE",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging role operation failure", e);
        }
    }

    /**
     * Log successful user permission operations
     */
    @AfterReturning(pointcut = USER_PERMISSION_OPERATIONS, returning = "result")
    public void logUserPermissionOperation(JoinPoint joinPoint, Object result) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "USER";
            String resourceId = getResourceId(joinPoint, result);
            String resourceName = getResourceName(joinPoint, result);
            String[] targets = getTargetInfo(joinPoint);

            String details = buildOperationDetails(joinPoint, result);

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                resourceName,
                targets[0], // targetUser
                targets[1], // targetRole
                targets[2], // targetPermission
                "SUCCESS",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging user permission operation", e);
        }
    }

    /**
     * Log failed user permission operations
     */
    @AfterThrowing(pointcut = USER_PERMISSION_OPERATIONS, throwing = "exception")
    public void logUserPermissionOperationFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "USER";
            String resourceId = getResourceId(joinPoint, null);

            String details = "Operation failed: " + exception.getMessage();

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                "FAILURE",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging user permission operation failure", e);
        }
    }

    /**
     * Log successful document permission operations
     */
    @AfterReturning(pointcut = DOCUMENT_PERMISSION_OPERATIONS, returning = "result")
    public void logDocumentPermissionOperation(JoinPoint joinPoint, Object result) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "DOCUMENT";
            String resourceId = getResourceId(joinPoint, result);
            String resourceName = getResourceName(joinPoint, result);
            String[] targets = getTargetInfo(joinPoint);

            String details = buildOperationDetails(joinPoint, result);

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                resourceName,
                targets[0], // targetUser
                targets[1], // targetRole
                targets[2], // targetPermission
                "SUCCESS",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging document permission operation", e);
        }
    }

    /**
     * Log failed document permission operations
     */
    @AfterThrowing(pointcut = DOCUMENT_PERMISSION_OPERATIONS, throwing = "exception")
    public void logDocumentPermissionOperationFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String operationType = getOperationType(joinPoint);
            String resourceType = "DOCUMENT";
            String resourceId = getResourceId(joinPoint, null);

            String details = "Operation failed: " + exception.getMessage();

            auditService.logPermissionOperation(
                operationType,
                resourceType,
                resourceId,
                "FAILURE",
                details
            );
        } catch (Exception e) {
            logger.error("Error logging document permission operation failure", e);
        }
    }

    /**
     * Extract operation type from method name
     */
    private String getOperationType(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();

        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.contains("modify")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("search")) {
            return "READ";
        } else {
            return methodName.toUpperCase();
        }
    }

    /**
     * Extract resource ID from method arguments or result
     */
    private String getResourceId(JoinPoint joinPoint, Object result) {
        // If result is an entity with ID, use that
        if (result != null) {
            try {
                // Try to extract ID from result (could be entity, Long, String, etc.)
                if (result instanceof Long) {
                    return result.toString();
                } else if (result instanceof String) {
                    return (String) result;
                } else if (result.getClass().getSimpleName().contains("Entity") ||
                          result.getClass().getSimpleName().contains("DTO")) {
                    // Try to find getId() method using reflection
                    java.lang.reflect.Method getIdMethod = null;
                    for (java.lang.reflect.Method method : result.getClass().getMethods()) {
                        if ("getId".equals(method.getName()) && method.getParameterCount() == 0) {
                            getIdMethod = method;
                            break;
                        }
                    }
                    if (getIdMethod != null) {
                        Object id = getIdMethod.invoke(result);
                        return id != null ? id.toString() : "UNKNOWN";
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not extract ID from result: {}", e.getMessage());
            }
        }

        // Otherwise, try to extract from method arguments
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            // Look for ID in the first argument (common pattern for update/delete operations)
            if (args[0] instanceof Long) {
                return args[0].toString();
            } else if (args[0] instanceof String) {
                return (String) args[0];
            }
        }

        return "UNKNOWN";
    }

    /**
     * Extract resource name from method arguments or result
     */
    private String getResourceName(JoinPoint joinPoint, Object result) {
        if (result != null) {
            try {
                // Try to find getName() method using reflection
                java.lang.reflect.Method getNameMethod = null;
                for (java.lang.reflect.Method method : result.getClass().getMethods()) {
                    if ("getName".equals(method.getName()) && method.getParameterCount() == 0) {
                        getNameMethod = method;
                        break;
                    }
                }
                if (getNameMethod != null) {
                    Object name = getNameMethod.invoke(result);
                    return name != null ? name.toString() : null;
                }
            } catch (Exception e) {
                logger.debug("Could not extract name from result: {}", e.getMessage());
            }
        }

        // Try to extract from method arguments
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof String) {
                // Look for strings that might be names/codes
                String str = (String) arg;
                if (str.length() < 100 && !isNumeric(str)) { // Likely a name rather than ID
                    return str;
                }
            }
        }

        return null;
    }

    /**
     * Extract target information from method arguments
     * Returns array: [targetUser, targetRole, targetPermission]
     */
    private String[] getTargetInfo(JoinPoint joinPoint) {
        String[] targets = {"", "", ""}; // [targetUser, targetRole, targetPermission]

        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        for (Object arg : args) {
            if (arg instanceof String) {
                String str = (String) arg;

                // Check if it looks like a username
                if (str.toLowerCase().contains("user") || str.length() < 50 && !isNumeric(str)) {
                    if (methodName.contains("user") || methodName.contains("User")) {
                        targets[0] = str;
                    }
                }
                // Check if it looks like a role
                else if (str.toLowerCase().contains("role") || str.toUpperCase().startsWith("ROLE_")) {
                    targets[1] = str;
                }
                // Check if it looks like a permission
                else if (str.contains(":") || str.toUpperCase().equals(str)) {
                    targets[2] = str;
                }
            }
        }

        return targets;
    }

    /**
     * Build operation details from method arguments
     */
    private String buildOperationDetails(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String argsDetails = Arrays.stream(args)
            .map(arg -> arg != null ? arg.toString() : "null")
            .collect(Collectors.joining(", "));

        String resultDetails = result != null ? result.toString() : "void";

        return String.format("Method: %s, Args: [%s], Result: %s",
                           methodName, argsDetails, resultDetails);
    }

    /**
     * Check if string is numeric
     */
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
