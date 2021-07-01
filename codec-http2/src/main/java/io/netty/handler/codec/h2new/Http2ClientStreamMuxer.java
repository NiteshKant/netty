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

public class Http2ClientStreamMuxer extends AbstractHttp2StreamMuxer {
    Http2ClientStreamMuxer(Http2Channel channel) {
        super(channel);
    }

    Http2ClientStreamMuxer(Http2Channel channel, DefaultChannelFlowControlledBytesDistributor defaultDistributor) {
        super(channel, defaultDistributor);
    }

    @Override
    protected void initPeerInitializedStream(Http2StreamChannel stream) {
        stream.pipeline().addLast(new Http2ClientRequestStreamInitializer());
        if (defaultDistributor != null) {
            stream.pipeline().addLast(new StreamFlowControlFrameInspector(defaultDistributor));
        }
        channel.pipeline().fireChannelRead(stream);
    }
}