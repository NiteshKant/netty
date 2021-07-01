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
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import static io.netty.handler.codec.h2new.DefaultHttp2ServerCodecBuilder.FLOW_CONTROLLED_BYTES_DISTRIBUTOR_ATTRIBUTE_KEY;

final class Http2ControlStreamInitializer extends ChannelHandlerAdapter {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2ControlStreamInitializer.class);

    private final Http2Settings initialSettings;
    private final ChannelFlowControlledBytesDistributor distributor;

    private boolean initialized;

    Http2ControlStreamInitializer(Http2Settings initialSettings,
                                  ChannelFlowControlledBytesDistributor flowControlledBytesDistributor) {
        this.initialSettings = initialSettings;
        this.distributor = flowControlledBytesDistributor;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        initialize(ctx);
        super.channelActive(ctx);
    }

    private void initialize(ChannelHandlerContext ctx) {
        if (initialized) {
            return;
        }

        initialized = true;
        setDistributorAttribute(ctx.channel());
        sendInitialSettings(ctx.channel());
    }

    private void setDistributorAttribute(Channel channel) {
        final ChannelFlowControlledBytesDistributor removed =
                channel.attr(FLOW_CONTROLLED_BYTES_DISTRIBUTOR_ATTRIBUTE_KEY)
                        .setIfAbsent(distributor);
        if (removed != null) {
            logger.debug("Failed to set attribute {}, attribute already exists {}.",
                    FLOW_CONTROLLED_BYTES_DISTRIBUTOR_ATTRIBUTE_KEY, removed);
        }
    }

    private void sendInitialSettings(Channel channel) {
        channel.writeAndFlush(initialSettings);
    }
}
