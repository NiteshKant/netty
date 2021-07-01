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

import io.netty.buffer.api.Buffer;
import io.netty.buffer.api.BufferHolder;

public class DefaultHttp2DataFrame extends BufferHolder<DefaultHttp2DataFrame> implements Http2DataFrame {
    private final Buffer payload;
    private final boolean endStream;
    private final int streamId;

    public DefaultHttp2DataFrame(int streamId, Buffer payload) {
        this(streamId, payload, false);
    }

    public DefaultHttp2DataFrame(int streamId, Buffer payload, boolean endStream) {
        super(payload);
        this.streamId = streamId;
        this.payload = payload;
        this.endStream = endStream;
    }

    @Override
    public Type frameType() {
        return Type.Headers;
    }

    @Override
    public byte flags() {
        return 0;
    }

    @Override
    public int streamId() {
        return streamId;
    }

    @Override
    public Buffer payload() {
        return payload;
    }

    @Override
    protected DefaultHttp2DataFrame receive(Buffer buf) {
        return new DefaultHttp2DataFrame(streamId, buf);
    }

    @Override
    public boolean isEndStream() {
        return endStream;
    }
}
