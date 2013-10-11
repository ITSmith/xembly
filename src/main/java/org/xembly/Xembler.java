/**
 * Copyright (c) 2013, xembly.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the xembly.org nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.xembly;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Processor of Xembly directives, main entry point to the package.
 *
 * <p>For example, to modify a DOM document:
 *
 * <pre> Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives()
 *     .xpath("/root")
 *     .addIfAbsent("employees")
 *     .add("employee")
 *     .attr("id", 6564)
 * ).apply(dom);</pre>
 *
 * <p>You can also convert your Xembly directives directly to XML document:
 *
 * <pre> String xml = new Xembler(
 *   new Directives()
 *     .xpath("/root")
 *     .addIfAbsent("employees")
 *     .add("employee")
 *     .attr("id", 6564)
 * ).xml("root");</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "directives")
@Loggable(Loggable.DEBUG)
public final class Xembler {

    /**
     * Array of directives.
     */
    private final transient Array<Directive> directives;

    /**
     * Public ctor.
     * @param dirs Directives
     */
    public Xembler(@NotNull(message = "collection of directives can't be NULL")
        final Collection<Directive> dirs) {
        this.directives = new Array<Directive>(dirs);
    }

    /**
     * Apply all changes to the document.
     * @param dom DOM document
     * @throws ImpossibleModificationException If can't modify
     */
    @Loggable(
        value = Loggable.DEBUG,
        ignore = ImpossibleModificationException.class
    )
    public void apply(@NotNull(message = "DOM can't be NULL")
        final Document dom) throws ImpossibleModificationException {
        final Node root = dom.getDocumentElement();
        if (root == null) {
            throw new ImpossibleModificationException(
                "DOM document doesn't have a root/document element"
            );
        }
        Collection<Node> ptr = Arrays.<Node>asList(root);
        int pos = 1;
        for (Directive dir : this.directives) {
            try {
                ptr = dir.exec(dom, ptr);
            } catch (ImpossibleModificationException ex) {
                throw new ImpossibleModificationException(
                    String.format("directive #%d: %s", pos, dir),
                    ex
                );
            }
            ++pos;
        }
    }

    /**
     * Apply all changes to an empty DOM with the given root element name.
     * @param name Name of root DOM element
     * @return DOM created
     * @throws ImpossibleModificationException If can't modify
     * @since 0.9
     */
    @Loggable(
        value = Loggable.DEBUG,
        ignore = ImpossibleModificationException.class
    )
    public Document dom(@NotNull(message = "root element name can't be NULL")
        final String name) throws ImpossibleModificationException {
        final Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        dom.appendChild(dom.createElement(name));
        this.apply(dom);
        return dom;
    }

    /**
     * Convert to XML document.
     * @param name Name of root DOM element
     * @return XML document
     * @throws ImpossibleModificationException If can't modify
     * @since 0.9
     */
    @Loggable(
        value = Loggable.DEBUG,
        ignore = ImpossibleModificationException.class
    )
    public String xml(@NotNull(message = "root element name can't be NULL")
        final String name) throws ImpossibleModificationException {
        final Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        final StringWriter writer = new StringWriter();
        try {
            transformer.transform(
                new DOMSource(this.dom(name)),
                new StreamResult(writer)
            );
        } catch (TransformerException ex) {
            throw new IllegalArgumentException(ex);
        }
        return writer.toString();
    }

}
