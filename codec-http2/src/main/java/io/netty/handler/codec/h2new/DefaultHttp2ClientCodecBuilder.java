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
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http2.Http2HeadersEncoder.SensitivityDetector;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;

public class DefaultHttp2ClientCodecBuilder implements Http2ClientCodecBuilder {
    private Function<Channel, ChannelFlowControlledBytesDistributor> distributorFactory;
    private Http2Settings initialSettings;
    private Http2ServerSslContext sslContext;

    @Override
    public Http2ClientCodecBuilder maxReservedStreams(int maxReservedStreams) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder validateHeaders(boolean validateHeaders) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder encoderEnforceMaxQueuedControlFrames(int maxQueuedControlFrames) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder headerSensitivityDetector(SensitivityDetector headerSensitivityDetector) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder autoAckSettingsFrame(boolean autoAckSettings) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder autoAckPingFrame(boolean autoAckPingFrame) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder decoupleCloseAndGoAway(boolean decoupleCloseAndGoAway) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder decoderEnforceMaxConsecutiveEmptyDataFrames(int maxConsecutiveEmptyFrames) {
        return null;
    }

    @Override
    public Http2ClientCodecBuilder initialSettings(Http2Settings settings) {
        this.initialSettings = settings;
        return this;
    }

    @Override
    public Http2ClientCodecBuilder sslContext(Http2ClientSslContext sslContext) {
        return null;
    }

    public Http2ClientCodecBuilder channelFlowControlledBytesDistributor(
            Function<Channel, ChannelFlowControlledBytesDistributor> distributorFactory) {
        this.distributorFactory = distributorFactory;
        return this;
    }

    @Override
    public ChannelHandler buildRaw(ChannelHandler handler) {
        checkNotNullWithIAE(handler, "handler");

        final Http2Settings settings = initialSettings;
        final Function<Channel, ChannelFlowControlledBytesDistributor> distributorFactory;
        if (this.distributorFactory == null) {
            distributorFactory = channel -> {
                DefaultChannelFlowControlledBytesDistributor distributor =
                        new DefaultChannelFlowControlledBytesDistributor(channel);
                channel.pipeline().addLast(distributor);
                channel.pipeline().addLast(new StreamFlowControlFrameInspector(distributor));
                return distributor;
            };
        } else {
            distributorFactory = this.distributorFactory;
        }

        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new Http2FrameCodec());
                final ChannelFlowControlledBytesDistributor distributor = distributorFactory.apply(ch);
                ch.pipeline().addLast(new Http2ControlStreamInitializer(settings, distributor));
                ch.pipeline().addLast(handler);
            }
        };
    }

    @Override
    public ChannelHandler build(BiConsumer<Channel, Http2Channel> initializer) {
        checkNotNullWithIAE(initializer, "initializer");
        final Http2Settings settings = initialSettings;
        final Function<Channel, ChannelFlowControlledBytesDistributor> distributorFactory;
        if (this.distributorFactory == null) {
            distributorFactory = channel -> {
                DefaultChannelFlowControlledBytesDistributor distributor =
                        new DefaultChannelFlowControlledBytesDistributor(channel);
                channel.pipeline().addLast(distributor);
                return distributor;
            };
        } else {
            distributorFactory = this.distributorFactory;
        }
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new Http2FrameCodec());
                final ChannelFlowControlledBytesDistributor distributor = distributorFactory.apply(ch);
                ch.pipeline().addLast(new Http2ControlStreamInitializer(settings, distributor));

                DefaultHttp2Channel h2channel = new DefaultHttp2Channel(ch, distributor, false);
                Http2ClientStreamMuxer muxer = distributor instanceof DefaultChannelFlowControlledBytesDistributor ?
                        new Http2ClientStreamMuxer(h2channel,
                                (DefaultChannelFlowControlledBytesDistributor) distributor) :
                        new Http2ClientStreamMuxer(h2channel);
                h2channel.pipeline().addLast(muxer);

                initializer.accept(ch, h2channel);
            }
        };
    }
}
