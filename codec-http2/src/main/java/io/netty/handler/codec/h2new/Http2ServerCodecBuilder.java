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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http2.Http2HeadersEncoder.SensitivityDetector;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface Http2ServerCodecBuilder {

    Http2ServerCodecBuilder maxReservedStreams(int maxReservedStreams);

    Http2ServerCodecBuilder validateHeaders(boolean validateHeaders);

    Http2ServerCodecBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams);

    Http2ServerCodecBuilder encoderEnforceMaxQueuedControlFrames(int maxQueuedControlFrames);

    Http2ServerCodecBuilder headerSensitivityDetector(SensitivityDetector headerSensitivityDetector);

    Http2ServerCodecBuilder encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize);

    Http2ServerCodecBuilder autoAckSettingsFrame(boolean autoAckSettings);

    Http2ServerCodecBuilder autoAckPingFrame(boolean autoAckPingFrame);

    Http2ServerCodecBuilder decoupleCloseAndGoAway(boolean decoupleCloseAndGoAway);

    Http2ServerCodecBuilder decoderEnforceMaxConsecutiveEmptyDataFrames(int maxConsecutiveEmptyFrames);

    Http2ServerCodecBuilder initialSettings(Http2Settings settings);

    Http2ServerCodecBuilder sslContext(Http2ServerSslContext sslContext);

    // Upgrade allowed for plain text only
    // https://httpwg.org/specs/rfc7540.html#rfc.section.3.2
    Http2ServerCodecBuilder supportUpgradeFromHttp1x();

    Http2ServerCodecBuilder supportUpgradeFromHttp1x(BiFunction<Channel, FullHttpRequest, Http2Settings> upgrade);

    ChannelHandler buildRaw(ChannelHandler handler);

    ChannelHandler build(Consumer<Http2Channel> initializer);
}
