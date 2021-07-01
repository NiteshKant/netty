/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.h2new;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;

import java.net.SocketAddress;

import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;

final class DefaultHttp2StreamChannel extends AbstractChannel implements Http2StreamChannel {
    private final Http2Channel parent;
    private final int streamId;
    private final ChannelFlowControlledBytesDistributor distributor;

    DefaultHttp2StreamChannel(Http2Channel parent, int streamId, ChannelFlowControlledBytesDistributor distributor) {
        super(parent, parent.eventLoop());
        this.parent = parent;
        this.streamId = streamId;
        this.distributor = checkNotNullWithIAE(distributor, "distributor");
    }

    @Override
    public int streamId() {
        return streamId;
    }

    @Override
    public Http2Channel parent() {
        return parent;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected SocketAddress localAddress0() {
        return parent.localAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return parent.remoteAddress();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
    }

    @Override
    protected void doDisconnect() throws Exception {
    }

    @Override
    protected void doClose() throws Exception {
    }

    @Override
    protected void doBeginRead() throws Exception {
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }
}
