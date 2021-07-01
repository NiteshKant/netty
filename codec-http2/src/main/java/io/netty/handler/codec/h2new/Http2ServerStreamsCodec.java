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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;

/**
 * A {@link ChannelHandler} that {@link #channelRead(ChannelHandlerContext, Object) reads} {@link Http2StreamChannel}
 * messages and configures such streams.
 */
public abstract class Http2ServerStreamsCodec extends ChannelHandlerAdapter implements Consumer<Http2Channel> {
    private final ChannelHandler controlStreamHandler;

    public Http2ServerStreamsCodec() {
        controlStreamHandler = null;
    }

    protected Http2ServerStreamsCodec(ChannelHandler controlStreamHandler) {
        this.controlStreamHandler = checkNotNullWithIAE(controlStreamHandler, "controlStreamHandler");
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Http2StreamChannel)) {
            ctx.fireChannelRead(msg);
            return;
        }

        Http2StreamChannel stream = (Http2StreamChannel) msg;
        if (stream.streamId() == 0 && controlStreamHandler != null) {
            stream.pipeline().addLast(controlStreamHandler);
        } else {
            handleRequestStream(stream);
        }
    }

    @Override
    public void accept(Http2Channel channel) {
        channel.pipeline().addLast(this);
    }

    protected abstract void handleRequestStream(Http2StreamChannel stream);
}
