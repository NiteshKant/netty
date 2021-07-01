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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http2.Http2PriorityFrame;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

final class DefaultChannelFlowControlledBytesDistributor extends ChannelHandlerAdapter
        implements ChannelFlowControlledBytesDistributor {
    private final IntObjectMap<Object> acceptors = new IntObjectHashMap<>();
    private final EventLoop eventLoop;

    DefaultChannelFlowControlledBytesDistributor(Channel channel) {
        eventLoop = channel.eventLoop();
    }

    @Override
    public DistributionAcceptor replace(int streamId, DistributionAcceptor acceptor) {
        if (!eventLoop.inEventLoop()) {
            throw new IllegalArgumentException("Invalid caller, not on eventloop: " + eventLoop);
        }

        final Object replaced = acceptors.replace(streamId, acceptor);
        if (replaced == null) {
            // early registration
            acceptors.put(streamId, new EarlyAcceptor(acceptor));
            return null;
        }

        if (replaced instanceof Integer) {
            acceptor.accumulate((Integer) replaced);
            return null;
        }

        assert replaced instanceof DistributionAcceptor;
        final DistributionAcceptor replacedAcceptor = (DistributionAcceptor) replaced;
        acceptor.accumulate(replacedAcceptor.dispose());
        return replacedAcceptor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Http2Frame)) {
            ctx.fireChannelRead(msg);
            return;
        }

        Http2Frame http2Frame = (Http2Frame) msg;
        final int streamId = http2Frame.streamId();
        boolean dispose = false;
        switch (http2Frame.frameType()) {
            case Headers:
                final Http2HeadersFrame headersFrame = (Http2HeadersFrame) http2Frame;
                acceptors.compute(streamId, (id, existing) -> {
                    if (existing == null) {
                        return forNewStream(headersFrame);
                    }
                    if (existing instanceof DistributionAcceptor) {
                        DistributionAcceptor acceptor = (DistributionAcceptor) existing;
                        if (acceptor instanceof EarlyAcceptor) {
                            acceptor.accumulate(forNewStream(headersFrame));
                        }
                        return existing;
                    }
                    assert existing instanceof Integer;
                    return existing;
                });
                break;
            case Priority:
                handleReprioritization((Http2PriorityFrame) http2Frame);
                break;
            case RstStream:
                dispose = true;
                break;
            case Settings:
                break;
            case WindowUpdate:
                break;
        }

        try {
            ctx.fireChannelRead(msg);
        } finally {
            if (dispose) {
                final Object maybeAcceptor = acceptors.remove(streamId);
                if (maybeAcceptor instanceof DistributionAcceptor) {
                    handleLeftOverBytes(streamId, ((DistributionAcceptor) maybeAcceptor).dispose());
                }
            }
        }
    }

    void bytesWritten(int streamId, int bytes) {
    }

    void streamOutputClosed(int streamId) {
        // TODO: Can get WINDOW_UPDATE post EOS, handle that.
        // https://httpwg.org/specs/rfc7540.html#WINDOW_UPDATE

        final Object removed = acceptors.remove(streamId);
        if (removed == null) {
            return;
        }
        int leftover;
        if (removed instanceof DistributionAcceptor) {
            leftover = ((DistributionAcceptor) removed).dispose();
        } else {
            assert removed instanceof Integer;
            leftover = (int) removed;
        }
        handleLeftOverBytes(streamId, leftover);
    }

    private void handleReprioritization(Http2PriorityFrame http2Frame) {
    }

    private void handleLeftOverBytes(int streamId, int dispose) {
    }

    private int forNewStream(Http2HeadersFrame headersFrame) {
        // check priority, weights and distribute
        return 0;
    }

    private static final class EarlyAcceptor implements DistributionAcceptor {
        private final DistributionAcceptor delegate;

        private EarlyAcceptor(DistributionAcceptor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void accumulate(int accumulate) {
            delegate.accumulate(accumulate);
        }

        @Override
        public int dispose() {
            return delegate.dispose();
        }
    }
}
