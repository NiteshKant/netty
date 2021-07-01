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
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.net.SocketAddress;

final class DefaultHttp2Channel implements Http2Channel {
    private final Channel delegate;
    private final ChannelFlowControlledBytesDistributor distributor;

    DefaultHttp2Channel(Channel delegate, ChannelFlowControlledBytesDistributor distributor, boolean isServer) {
        this.delegate = delegate;
        this.distributor = distributor;
    }

    @Override
    public Future<Http2StreamChannel> createStream(ChannelHandler handler) {
        final Promise<Http2StreamChannel> promise = eventLoop().newPromise();
        createStream(handler, promise);
        return promise;
    }

    @Override
    public void createStream(ChannelHandler handler, Promise<Http2StreamChannel> promise) {
        // TODO: generate stream ID and optionally defer creation till allowed
        DefaultHttp2StreamChannel stream = new DefaultHttp2StreamChannel(this, 1, distributor);
        pipeline().write(stream);
        stream.pipeline().addLast(handler);
        promise.setSuccess(stream);
    }

    Channel delegate() {
        return delegate;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return delegate.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return delegate.hasAttr(key);
    }

    @Override
    public ChannelId id() {
        return delegate.id();
    }

    @Override
    public EventLoop eventLoop() {
        return delegate.eventLoop();
    }

    @Override
    public Channel parent() {
        return delegate.parent();
    }

    @Override
    public ChannelConfig config() {
        return delegate.config();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return delegate.isRegistered();
    }

    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return delegate.metadata();
    }

    @Override
    public SocketAddress localAddress() {
        return delegate.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return delegate.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture() {
        return delegate.closeFuture();
    }

    @Override
    public boolean isWritable() {
        return delegate.isWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return delegate.bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return delegate.bytesBeforeWritable();
    }

    @Override
    public Unsafe unsafe() {
        return delegate.unsafe();
    }

    @Override
    public ChannelPipeline pipeline() {
        return delegate.pipeline();
    }

    @Override
    public ByteBufAllocator alloc() {
        return delegate.alloc();
    }

    @Override
    public Channel read() {
        return delegate.read();
    }

    @Override
    public Channel flush() {
        return delegate.flush();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return delegate.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return delegate.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return delegate.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return delegate.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return delegate.close();
    }

    @Override
    public ChannelFuture register() {
        return delegate.register();
    }

    @Override
    public ChannelFuture deregister() {
        return delegate.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return delegate.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return delegate.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return delegate.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return delegate.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return delegate.close(promise);
    }

    @Override
    public ChannelFuture register(ChannelPromise promise) {
        return delegate.register(promise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return delegate.deregister(promise);
    }

    @Override
    public ChannelFuture write(Object msg) {
        return delegate.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return delegate.write(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return delegate.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return delegate.writeAndFlush(msg);
    }

    @Override
    public ChannelPromise newPromise() {
        return delegate.newPromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return delegate.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return delegate.newFailedFuture(cause);
    }

    @Override
    public int compareTo(Channel o) {
        return delegate.compareTo(o);
    }
}
