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
import io.netty.buffer.api.Buffer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.h2new.DefaultHttp2ClientCodecBuilder;
import io.netty.handler.codec.h2new.DefaultHttp2ClientSslContextBuilder;
import io.netty.handler.codec.h2new.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.h2new.Http2ClientChannelInitializer;
import io.netty.handler.codec.h2new.Http2ClientCodecBuilder;
import io.netty.handler.codec.h2new.Http2DataFrame;
import io.netty.handler.codec.h2new.Http2Frame;
import io.netty.handler.codec.h2new.Http2HeadersFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_MAX_CONCURRENT_STREAMS;
import static io.netty.handler.codec.http2.Http2Headers.PseudoHeaderName.PATH;
import static io.netty.util.ReferenceCountUtil.release;

public class Http2ClientExampleNoStreams {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new MultithreadEventLoopGroup(NioHandler.newFactory());
        try {
            Http2ClientCodecBuilder codecBuilder = new DefaultHttp2ClientCodecBuilder()
                    .sslContext(new DefaultHttp2ClientSslContextBuilder().build())
                    .initialSettings(new Http2Settings());

            CountDownLatch awaitMaxConurrentStreamsSettings = new CountDownLatch(1);
            // Below is to be done per channel
            Http2ClientChannelInitializer channelInitializer =
                    new Http2ClientChannelInitializer(new ChannelHandlerAdapter() {
                        private final Map<Integer, StreamListener> streams = new HashMap<>();

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (!(msg instanceof Http2Frame)) {
                                release(msg);
                                return;
                            }
                            Http2Frame http2Frame = (Http2Frame) msg;
                            if (http2Frame instanceof Http2SettingsFrame) {
                                Http2Settings settings = ((Http2SettingsFrame) http2Frame).settings();
                                long maxConcurrentStreams = settings.get(SETTINGS_MAX_CONCURRENT_STREAMS);
                                if (maxConcurrentStreams >= 2) {
                                    awaitMaxConurrentStreamsSettings.countDown();
                                }
                            }
                            StreamListener processor;
                            final int streamId = http2Frame.streamId();
                            if (streamId == 0) {
                                // control stream, ignore
                                release(http2Frame);
                            }
                            switch (http2Frame.frameType()) {
                                case Data:
                                    processor = streams.get(streamId);
                                    assert processor != null;
                                    Http2DataFrame dataFrame = (Http2DataFrame) http2Frame;
                                    processor.data(dataFrame.payload());
                                    if (dataFrame.isEndStream()) {
                                        processor.end();
                                    }
                                    break;
                                case Headers:
                                    processor = streams.get(streamId);
                                    if (processor == null) {
                                        // headers
                                        processor = new StreamListener(streamId);
                                        streams.put(streamId, processor);
                                        processor.headers(((Http2HeadersFrame) http2Frame).headers());
                                    } else {
                                        // trailers
                                        streams.remove(streamId);
                                        processor.end();
                                    }
                                    break;
                                case RstStream:
                                    processor = streams.get(streamId);
                                    processor.reset();
                                    break;
                                default:
                                    release(msg);
                            }
                        }
                    });
            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(codecBuilder.buildRaw(channelInitializer))
                    .remoteAddress("127.0.0.1", 8080)
                    .connect().sync().channel();

            awaitMaxConurrentStreamsSettings.await();

            channel.writeAndFlush(new DefaultHttp2HeadersFrame(channelInitializer.nextStreamId(),
                    new DefaultHttp2Headers().add(PATH.name(), "/foo"), true));

            channel.writeAndFlush(new DefaultHttp2HeadersFrame(channelInitializer.nextStreamId(),
                    new DefaultHttp2Headers().add(PATH.name(), "/bar"), true));
        } finally {
            group.shutdownGracefully();
        }
    }

    private static final class StreamListener {
        private final int streamId;

        StreamListener(int streamId) {
            this.streamId = streamId;
        }

        void headers(Http2Headers headers) {
            System.out.println("Stream id:" + streamId + ", headers: " + headers);
        }

        void data(Buffer buffer) {
            System.out.println("Stream id:" + streamId + ", data: " + buffer.toString());
            buffer.close();
        }

        void end() {
            System.out.println("Stream id:" + streamId + ", response done!");
        }

        void reset() {
            // noop
        }
    }
}
