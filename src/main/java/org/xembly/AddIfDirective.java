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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ADDIF directive.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@EqualsAndHashCode(of = "name")
@Loggable(Loggable.DEBUG)
final class AddIfDirective implements Directive {

    /**
     * Name of node to add.
     */
    private final transient Arg name;

    /**
     * Public ctor.
     * @param node Name of node to add
     * @throws XmlContentException If invalid input
     */
    protected AddIfDirective(final String node) throws XmlContentException {
        this.name = new Arg(node.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return String.format("ADDIF %s", this.name);
    }

    @Override
    public Collection<Node> exec(final Document dom,
        final Collection<Node> nodes) {
        final Collection<Node> dests = new ArrayList<Node>(nodes.size());
        for (Node node : nodes) {
            final NodeList childs = node.getChildNodes();
            Node dest = null;
            for (int idx = 0; idx < childs.getLength(); ++idx) {
                if (childs.item(idx).getNodeName()
                    .toLowerCase(Locale.ENGLISH).equals(this.name.raw())) {
                    dest = childs.item(idx);
                    break;
                }
            }
            if (dest == null) {
                dest = dom.createElement(this.name.raw());
                node.appendChild(dest);
            }
            dests.add(dest);
        }
        return Collections.unmodifiableCollection(dests);
    }

}
