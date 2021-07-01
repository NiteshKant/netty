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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.function.BiConsumer;

public final class Http2ClientMuxedChannelInitializer implements BiConsumer<Channel, Http2Channel> {

    private final ChannelHandler controlStreamHandler;
    private Channel delegateChannel; // memory fence provided by http2Channel
    private volatile Http2Channel http2Channel;

    public Http2ClientMuxedChannelInitializer() {
        this(new ChannelHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ReferenceCountUtil.release(msg);
            }
        });
    }

    public Http2ClientMuxedChannelInitializer(ChannelHandler controlStreamHandler) {
        this.controlStreamHandler = controlStreamHandler;
    }

    public Http2Channel asHttp2Channel(Channel channel) {
        final Http2Channel http2Channel = this.http2Channel;
        if (delegateChannel != channel) {
            throw new IllegalArgumentException("Provided channel: " + channel +
                    " is different than the configured channel: " + delegateChannel);
        }
        return http2Channel;
    }

    @Override
    public void accept(Channel channel, Http2Channel http2Channel) {
        delegateChannel = channel;
        this.http2Channel = http2Channel;
    }
}
