package org.eclipse.cdt.testsrunner.internal.catch_test.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Expressions are produced by the majority of Catch <code>REQUIRE(expr)</code>
 * and <code>CHECK(expr)</code> operations.
 */
public class Expression {
    @XmlElement(name = "Original")
    private String original;
    @XmlElement(name = "Expanded")
    private String expanded;
    @XmlAttribute
    private String filename;
    @XmlAttribute
    private int line;

    /**
     * Provides the tested {@link String expression} as written in the source
     * code.
     *
     * @return Original {@link String expression}
     */
    public String getOriginal() {
        return original;
    }

    /**
     * Provides the expanded {@link String expression}, e.g. actually evaluated
     * values instead of variable names.
     *
     * @return Expanded {@link String expression}
     */
    public String getExpanded() {
        return expanded;
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
     * The exact line the expression was asserted on. This may differ from the
     * test case line number.
     *
     * @return <code>this</code> expression's line number.
     */
    public int getLine() {
        return line;
    }
}
