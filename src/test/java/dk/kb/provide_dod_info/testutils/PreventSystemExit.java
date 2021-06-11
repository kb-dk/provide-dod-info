package dk.kb.provide_dod_info.testutils;

import java.security.Permission;

public class PreventSystemExit {

    public static class ExitTrappedException extends SecurityException { }

    public static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if( permission.getName().startsWith("exitVM") ) {
                    throw new ExitTrappedException() ;
                }
            }
        } ;
        System.setSecurityManager( securityManager ) ;
        System.out.println("Exit disabled");
    }

    public static void enableSystemExitCall() {
        System.setSecurityManager( null ) ;
    }
}
