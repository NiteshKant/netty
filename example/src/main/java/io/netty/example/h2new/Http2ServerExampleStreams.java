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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.h2new.DefaultHttp2ServerCodecBuilder;
import io.netty.handler.codec.h2new.DefaultHttp2ServerSslContextBuilder;
import io.netty.handler.codec.h2new.Http2RequestStreamInboundHandler;
import io.netty.handler.codec.h2new.Http2ServerCodecBuilder;
import io.netty.handler.codec.h2new.Http2ServerStreamsCodec;
import io.netty.handler.codec.h2new.Http2StreamChannel;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class Http2ServerExampleStreams {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new MultithreadEventLoopGroup(NioHandler.newFactory());
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            final DefaultHttp2ServerSslContextBuilder sslContextBuilder =
                    new DefaultHttp2ServerSslContextBuilder(ssc.certificate(), ssc.privateKey());
            Http2ServerCodecBuilder codecBuilder = new DefaultHttp2ServerCodecBuilder()
                    .sslContext(sslContextBuilder.build())
                    .initialSettings(new Http2Settings());

            final ChannelHandler codec = codecBuilder.build(new Http2ServerStreamsCodec() {
                        @Override
                        protected void handleRequestStream(Http2StreamChannel stream) {
                            stream.pipeline().addLast(new Http2RequestStreamInboundHandler() {
                                @Override
                                protected void handleHeaders(Http2HeadersFrame headersFrame) {
                                    stream.writeAndFlush(new DefaultHttp2Headers());
                                }

                                @Override
                                protected void handleData(Http2DataFrame dataFrame) {
                                    stream.writeAndFlush(dataFrame);
                                }
                            });
                        }
                    });

            new ServerBootstrap()
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(codec)
                    .bind(8080).sync().channel()
                    .closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
