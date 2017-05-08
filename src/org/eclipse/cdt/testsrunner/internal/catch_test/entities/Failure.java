package org.eclipse.cdt.testsrunner.internal.catch_test.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Failures are produced by Catch <code>FAIL(message)</code> statements.
 */
public class Failure {
    @XmlAttribute
    private String filename;
    @XmlAttribute
    private int line;
    @XmlValue
    private String message;

    /**
     * The {@link String message} provided as an argument to the
     * <code>FAIL(expr)</code> operation.
     *
     * @return The fail {@link String message}
     */
    public String getMessage() {
        return message;
    }

    /**
     * The file name <code>this</code> fail occurred. Matches the associated
     * test case's file name in almost all cases.
     *
     * @return <code>this</code> fail's file name.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * The exact line the fail occurred on. This may differ from the test case
     * line number.
     *
     * @return <code>this</code> fail's line number.
     */
    public int getLine() {
        return line;
    }
}
