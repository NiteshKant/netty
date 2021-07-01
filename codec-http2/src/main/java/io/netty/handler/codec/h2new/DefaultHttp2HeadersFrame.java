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

import io.netty.handler.codec.http2.Http2Headers;

public class DefaultHttp2HeadersFrame implements Http2HeadersFrame {
    private final Http2Headers headers;
    private final int streamId;
    private final boolean endStream;

    public DefaultHttp2HeadersFrame(int streamId, Http2Headers headers) {
        this(streamId, headers, false);
    }

    public DefaultHttp2HeadersFrame(int streamId, Http2Headers headers, boolean endStream) {
        this.headers = headers;
        this.streamId = streamId;
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
    public Http2Headers headers() {
        return headers;
    }

    @Override
    public boolean isEndStream() {
        return endStream;
    }
}
