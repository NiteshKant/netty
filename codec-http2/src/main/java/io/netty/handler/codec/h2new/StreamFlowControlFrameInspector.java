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

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

final class StreamFlowControlFrameInspector extends ChannelHandlerAdapter {
    private final DefaultChannelFlowControlledBytesDistributor distributor;

    StreamFlowControlFrameInspector(DefaultChannelFlowControlledBytesDistributor distributor) {
        this.distributor = distributor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!(msg instanceof Http2Frame)) {
            ctx.write(msg, promise);
            return;
        }

        Http2Frame frame = (Http2Frame) msg;
        switch (frame.frameType()) {
            case Data:
                final Http2DataFrame dataFrame = (Http2DataFrame) frame;
                distributor.bytesWritten(frame.streamId(), dataFrame.payload().readableBytes());
                if (dataFrame.isEndStream()) {
                    distributor.streamOutputClosed(frame.streamId());
                }
                break;
            case Headers:
                if (((Http2HeadersFrame) frame).isEndStream()) {
                    distributor.streamOutputClosed(frame.streamId());
                }
                break;
            case RstStream:
                distributor.streamOutputClosed(frame.streamId());
                break;
        }

        ctx.write(msg, promise);
    }
}
