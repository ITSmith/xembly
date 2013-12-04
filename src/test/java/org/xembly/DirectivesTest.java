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

import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.XMLDocument;
import com.rexsl.test.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link Directives}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DirectivesTest {

    /**
     * Directives can parse xembly grammar.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesIncomingGrammar() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '//orders[@id=\"152\"]'; SET 'test';"
        );
        MatcherAssert.assertThat(
            dirs,
            Matchers.<Directive>iterableWithSize(2)
        );
    }

    /**
     * Directives can throw when grammar is broken.
     * @throws Exception If some problem inside
     */
    @Test(expected = SyntaxException.class)
    public void throwsOnBrokenGrammar() throws Exception {
        new Directives("not a xembly at all");
    }

    /**
     * Directives can throw when XML content is broken.
     * @throws Exception If some problem inside
     */
    @Test(expected = SyntaxException.class)
    public void throwsOnBrokenXmlContent() throws Exception {
        new Directives("ADD '\u001b';");
    }

    /**
     * Directives can throw when escaped XML content is broken.
     * @throws Exception If some problem inside
     */
    @Test(expected = SyntaxException.class)
    public void throwsOnBrokenEscapedXmlContent() throws Exception {
        new Directives("ADD '&#27;';");
    }

    /**
     * Directives can add map of values.
     * @throws Exception If some problem inside
     * @since 0.8
     */
    @Test
    public void addsMapOfValues() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("root"));
        new Xembler(
            new Directives().xpath("/root").add(
                new ArrayMap<String, Object>()
                    .with("first", 1)
                    .with("second", "two")
            ).add("third")
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/first[.=1]",
                "/root/second[.='two']",
                "/root/third"
            )
        );
    }

    /**
     * Directives can ignore empty input.
     * @throws Exception If some problem inside
     */
    @Test
    public void ingoresEmptyInput() throws Exception {
        MatcherAssert.assertThat(
            new Directives("\n\t   \r"),
            Matchers.emptyIterable()
        );
    }

    /**
     * Directives can build a correct modification programme.
     * @throws Exception If some problem inside
     */
    @Test
    public void performsFullScaleModifications() throws Exception {
        final String script = new Directives()
            .add("html").attr("xmlns", "http://www.w3.org/1999/xhtml")
            .add("body")
            .add("p")
            .set("\u20ac \\")
            .toString();
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(new Directives(script)).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html",
                "/xhtml:html/body/p[.='\u20ac \\']"
            )
        );
    }

    /**
     * Directives can copy an existing node.
     * @throws Exception If some problem inside
     * @since 0.13
     */
    @Test
    public void copiesExistingNode() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives().add("dudes").append(
                Directives.copyOf(
                    new XMLDocument(
                        StringUtils.join(
                            "<jeff name='Jeffrey'><first/><second/>",
                            "<?some-pi test?>",
                            "<file a='x'><f><name>\u20ac</name></f></file>",
                            "<!-- some comment -->",
                            "<x><![CDATA[hey you]]></x>  </jeff>"
                        )
                    ).node()
                )
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/dudes/jeff[@name = 'Jeffrey']",
                "/dudes/jeff[first and second]",
                "/dudes/jeff/file[@a='x']/f[name='\u20ac']"
            )
        );
    }

}
