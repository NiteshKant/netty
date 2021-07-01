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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http2.Http2HeadersEncoder.SensitivityDetector;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.AttributeKey;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;

public class DefaultHttp2ServerCodecBuilder implements Http2ServerCodecBuilder {
    static final AttributeKey<ChannelFlowControlledBytesDistributor>
            FLOW_CONTROLLED_BYTES_DISTRIBUTOR_ATTRIBUTE_KEY =
            AttributeKey.newInstance("_netty.v5.h2.flow.controlled.bytes.distributor");
    private Function<Channel, ChannelFlowControlledBytesDistributor> distributorFactory;

    private Http2Settings initialSettings;
    private Http2ServerSslContext sslContext;

    @Override
    public Http2ServerCodecBuilder maxReservedStreams(int maxReservedStreams) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder validateHeaders(boolean validateHeaders) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder encoderEnforceMaxQueuedControlFrames(int maxQueuedControlFrames) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder headerSensitivityDetector(SensitivityDetector headerSensitivityDetector) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder autoAckSettingsFrame(boolean autoAckSettings) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder autoAckPingFrame(boolean autoAckPingFrame) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder decoupleCloseAndGoAway(boolean decoupleCloseAndGoAway) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder decoderEnforceMaxConsecutiveEmptyDataFrames(int maxConsecutiveEmptyFrames) {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder initialSettings(Http2Settings settings) {
        this.initialSettings = settings;
        return this;
    }

    @Override
    public Http2ServerCodecBuilder sslContext(Http2ServerSslContext sslContext) {
        this.sslContext = checkNotNullWithIAE(sslContext, "sslContext");
        return this;
    }

    @Override
    public Http2ServerCodecBuilder supportUpgradeFromHttp1x() {
        return null;
    }

    @Override
    public Http2ServerCodecBuilder supportUpgradeFromHttp1x(
            BiFunction<Channel, FullHttpRequest, Http2Settings> upgrade) {
        return null;
    }

    public Http2ServerCodecBuilder channelFlowControlledBytesDistributor(
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
    public ChannelHandler build(Consumer<Http2Channel> initializer) {
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

                DefaultHttp2Channel h2channel = new DefaultHttp2Channel(ch, distributor, true);
                AbstractHttp2StreamMuxer muxer = distributor instanceof DefaultChannelFlowControlledBytesDistributor ?
                        new Http2ServerStreamMuxer(h2channel,
                                (DefaultChannelFlowControlledBytesDistributor) distributor) :
                        new Http2ServerStreamMuxer(h2channel);
                h2channel.pipeline().addLast(muxer);

                initializer.accept(h2channel);
            }
        };
    }
}
