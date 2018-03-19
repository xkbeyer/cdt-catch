package org.eclipse.cdt.testsrunner.internal.catch_test;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * The activator class controls the plug-in life cycle
 */
public class CatchTestsRunnerPlugin extends Plugin {

   /** The plug-in ID . */
   public static final String PLUGIN_ID = "org.eclipse.cdt.testsrunner.catch_test"; //$NON-NLS-1$

   /** Plug-in instance. */
   private static CatchTestsRunnerPlugin plugin;

   public CatchTestsRunnerPlugin() {
      super();
      plugin = this;
   }

   /**
    * Returns the Catch Tests Runner provider plug-in instance.
    * 
    * @return the plug-in instance
    */
   public static CatchTestsRunnerPlugin getDefault() {
      return plugin;
   }

   /** Convenience method which returns the unique identifier of this plugin. */
   public static String getUniqueIdentifier() {
      return PLUGIN_ID;
   }

   /**
    * Logs the specified status with this plug-in's log.
    * 
    * @param status
    *           status to log
    */
   public static void log(IStatus status) {
      getDefault().getLog().log(status);
   }

   /**
    * Logs an internal error with the specified throwable
    * 
    * @param e
    *           the exception to be logged
    */
   public static void log(Throwable e) {
      log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
   }

}
