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
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * PI directive.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.9
 */
@Immutable
@EqualsAndHashCode(of = { "target", "data" })
final class PiDirective implements Directive {

    /**
     * Target name.
     */
    private final transient Arg target;

    /**
     * Data.
     */
    private final transient Arg data;

    /**
     * Public ctor.
     * @param tgt Target
     * @param dat Data
     * @throws XmlContentException If invalid input
     */
    PiDirective(final String tgt, final String dat)
        throws XmlContentException {
        this.target = new Arg(tgt.toLowerCase(Locale.ENGLISH));
        this.data = new Arg(dat);
    }

    @Override
    public String toString() {
        return String.format("PI %s, %s", this.target, this.data);
    }

    @Override
    public Collection<Node> exec(final Document dom,
        final Collection<Node> current) {
        final Node instr = dom.createProcessingInstruction(
            this.target.raw(), this.data.raw()
        );
        if (current.isEmpty()) {
            dom.insertBefore(instr, dom.getDocumentElement());
        } else {
            for (final Node node : current) {
                node.appendChild(instr);
            }
        }
        return Collections.unmodifiableCollection(current);
    }

}
