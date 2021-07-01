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
import io.netty.handler.codec.http2.Http2HeadersEncoder.SensitivityDetector;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.function.BiConsumer;

public interface Http2ClientCodecBuilder {
    Http2ClientCodecBuilder maxReservedStreams(int maxReservedStreams);

    Http2ClientCodecBuilder validateHeaders(boolean validateHeaders);

    Http2ClientCodecBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams);

    Http2ClientCodecBuilder encoderEnforceMaxQueuedControlFrames(int maxQueuedControlFrames);

    Http2ClientCodecBuilder headerSensitivityDetector(SensitivityDetector headerSensitivityDetector);

    Http2ClientCodecBuilder encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize);

    Http2ClientCodecBuilder autoAckSettingsFrame(boolean autoAckSettings);

    Http2ClientCodecBuilder autoAckPingFrame(boolean autoAckPingFrame);

    Http2ClientCodecBuilder decoupleCloseAndGoAway(boolean decoupleCloseAndGoAway);

    Http2ClientCodecBuilder decoderEnforceMaxConsecutiveEmptyDataFrames(int maxConsecutiveEmptyFrames);

    Http2ClientCodecBuilder initialSettings(Http2Settings settings);

    Http2ClientCodecBuilder sslContext(Http2ClientSslContext sslContext);

    ChannelHandler buildRaw(ChannelHandler handler);

    ChannelHandler build(BiConsumer<Channel, Http2Channel> initializer);
}
