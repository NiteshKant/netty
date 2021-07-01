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

package io.netty.example.h2new;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.h2new.DefaultHttp2ClientCodecBuilder;
import io.netty.handler.codec.h2new.DefaultHttp2ClientSslContextBuilder;
import io.netty.handler.codec.h2new.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.h2new.Http2Channel;
import io.netty.handler.codec.h2new.Http2ClientCodecBuilder;
import io.netty.handler.codec.h2new.Http2ClientMuxedChannelInitializer;
import io.netty.handler.codec.h2new.Http2RequestStreamInboundHandler;
import io.netty.handler.codec.h2new.Http2StreamChannel;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2Settings;

public class Http2ClientExampleStreams {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new MultithreadEventLoopGroup(NioHandler.newFactory());
        try {
            Http2ClientCodecBuilder codecBuilder =
                    new DefaultHttp2ClientCodecBuilder().sslContext(new DefaultHttp2ClientSslContextBuilder().build())
                            .initialSettings(new Http2Settings());

            Http2ClientMuxedChannelInitializer channelInitializer = new Http2ClientMuxedChannelInitializer();

            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(codecBuilder.build(channelInitializer))
                    .remoteAddress("127.0.0.1", 8080)
                    .connect().sync().channel();

            Http2Channel h2Channel = channelInitializer.asHttp2Channel(channel);
            Http2StreamChannel stream = h2Channel.createStream(new Http2RequestStreamInboundHandler() {
                @Override
                protected void handleHeaders(Http2HeadersFrame headersFrame) {
                    System.out.println(headersFrame);
                }

                @Override
                protected void handleData(Http2DataFrame dataFrame) {
                    dataFrame.release();
                    if (dataFrame.isEndStream()) {
                        System.out.println("Response done!");
                    }
                }
            }).sync().get();
            stream.writeAndFlush(new DefaultHttp2HeadersFrame(stream.streamId(), new DefaultHttp2Headers(), true));
        } finally {
            group.shutdownGracefully();
        }
    }
}
