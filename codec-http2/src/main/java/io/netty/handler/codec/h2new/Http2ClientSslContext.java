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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import java.util.List;

public final class Http2ClientSslContext extends SslContext {
    public boolean http1xAllowed() {
        return false;
    }

    public ChannelInitializer<Channel> http1xInitializer() {
        return null;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public List<String> cipherSuites() {
        return null;
    }

    @Override
    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return null;
    }

    @Override
    public SSLEngine newEngine(ByteBufAllocator alloc) {
        return null;
    }

    @Override
    public SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        return null;
    }

    @Override
    public SSLSessionContext sessionContext() {
        return null;
    }
}
