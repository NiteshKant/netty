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

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

import static io.netty.handler.codec.h2new.DefaultHttp2ServerCodecBuilder.FLOW_CONTROLLED_BYTES_DISTRIBUTOR_ATTRIBUTE_KEY;
import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;

abstract class AbstractHttp2StreamMuxer extends ChannelHandlerAdapter {
    protected final Http2Channel channel;
    protected final DefaultChannelFlowControlledBytesDistributor defaultDistributor;
    private final IntObjectMap<Http2StreamChannel> streams = new IntObjectHashMap<>();

    AbstractHttp2StreamMuxer(Http2Channel channel) {
        this.channel = checkNotNullWithIAE(channel, "channel");
        this.defaultDistributor = null;
    }

    AbstractHttp2StreamMuxer(Http2Channel channel, DefaultChannelFlowControlledBytesDistributor defaultDistributor) {
        this.channel = checkNotNullWithIAE(channel, "channel");
        this.defaultDistributor = checkNotNullWithIAE(defaultDistributor, "defaultDistributor");
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Http2Frame)) {
            ctx.fireChannelRead(msg);
            return;
        }

        Http2Frame http2Frame = (Http2Frame) msg;
        final Http2StreamChannel stream = createOrGetStream(http2Frame);

        stream.pipeline().fireChannelRead(http2Frame);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof Http2StreamChannel) {
            Http2StreamChannel stream = (Http2StreamChannel) msg;
            streams.put(stream.streamId(), stream);
            return;
        }
        ctx.write(msg, promise);
    }

    Http2StreamChannel createOrGetStream(Http2Frame frame) {
        Http2StreamChannel stream = streams.get(frame.streamId());
        if (stream != null) {
            return stream;
        }
        final Http2Frame.Type frameType = frame.frameType();
        if (frameType == Http2Frame.Type.Headers) {
            stream = new DefaultHttp2StreamChannel(channel, frame.streamId(),
                    channel.attr(FLOW_CONTROLLED_BYTES_DISTRIBUTOR_ATTRIBUTE_KEY).get());
            streams.put(stream.streamId(), stream);
            initPeerInitializedStream(stream);
        }
        // unknown stream, close connection
        throw new IllegalStateException("Unknown stream: " + frame.streamId());
    }

    protected abstract void initPeerInitializedStream(Http2StreamChannel stream);
}
