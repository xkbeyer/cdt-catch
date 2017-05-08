package org.eclipse.cdt.testsrunner.internal.catch_test.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Catch test suite node.
 */
public class TestCase {
    @XmlElement(name = "Expression")
    private Expression expression;
    @XmlElement(name = "Failure")
    private Failure failure;
    @XmlElement(name = "OverallResult")
    private OverallResult result;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String tags;
    @XmlAttribute
    private String filename;
    @XmlAttribute
    private int line;

    /**
     * Catch test suite XML nodes are either {@link Expression expressions} or
     * {@link Failure failures}.
     *
     * @return <code>true</code> if <code>this</code> test case contains an
     *         {@link Expression}.
     */
    public boolean hasExpression() {
        return expression != null;
    }

    /**
     * Provides <code>this</code> test case's {@link Expression}.
     *
     * @return <code>this</code> test case's {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Provides <code>this</code> test case's {@link Failure}.
     *
     * @return <code>this</code> test case's {@link Failure}
     */
    public Failure getFailure() {
        return failure;
    }

    /**
     * Provides <code>this</code> test case's {@link OverallResult result
     * summary}.
     *
     * @return <code>this</code> test case's {@link OverallResult result
     *         summary}.
     */
    public OverallResult getResult() {
        return result;
    }

    /**
     * Provides <code>this</code> test case's {@link String name}.
     *
     * @return <code>this</code> test case's {@link String name}.
     */
    public String getName() {
        return name;
    }

    /**
     * Catch test case tags are frequently used similar to Java package names,
     * but can be any arbitrary {@link String}.
     *
     * @return <code>this</code> test case's {@link String tags}.
     */
    public String getTags() {
        return tags;
    }

    /**
     * {@link String Name} of the file <code>this</code> test case is in.
     *
     * @return <code>this</code> test case's {@link String file name}.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * {@link Integer Line number} the test case is located on.
     *
     * @return <code>this</code> test case's {@link Integer line number}.
     */
    public int getLine() {
        return line;
    }
}
