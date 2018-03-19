package org.eclipse.cdt.testsrunner.internal.catch_test;

import org.eclipse.osgi.util.NLS;

public class CatchTestsRunnerMessages extends NLS {
   private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.catch_test.CatchTestsRunnerMessages"; //$NON-NLS-1$
   public static String CatchTestsRunner_error_format;
   public static String CatchTestsRunner_io_error_prefix;
   public static String CatchTestsRunner_wrong_tests_paths_count;
   static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, CatchTestsRunnerMessages.class);
   }

   private CatchTestsRunnerMessages() {}
}
