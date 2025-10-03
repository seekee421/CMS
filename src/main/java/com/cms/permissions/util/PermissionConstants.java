package com.cms.permissions.util;

public class PermissionConstants {
    // Document permissions
    public static final String DOC_EDIT = "DOC:EDIT";
    public static final String DOC_PUBLISH = "DOC:PUBLISH";
    public static final String DOC_APPROVE_ALL = "DOC:APPROVE:ALL";
    public static final String DOC_APPROVE_ASSIGNED = "DOC:APPROVE:ASSIGNED";
    public static final String DOC_VIEW_LOGGED = "DOC:VIEW:LOGGED";
    public static final String DOC_DOWNLOAD = "DOC:DOWNLOAD";
    public static final String DOC_CREATE = "DOC:CREATE";
    public static final String DOC_DELETE = "DOC:DELETE";
    public static final String DOC_ASSIGN = "DOC:ASSIGN";

    // Comment permissions
    public static final String COMMENT_CREATE = "COMMENT:CREATE";
    public static final String COMMENT_MANAGE = "COMMENT:MANAGE";
    public static final String COMMENT_READ = "COMMENT:READ";

    // User management permissions
    public static final String USER_MANAGE_SUB = "USER:MANAGE:SUB";
    public static final String USER_MANAGE_EDITOR = "USER:MANAGE:EDITOR";
    public static final String USER_READ = "USER:READ";
}