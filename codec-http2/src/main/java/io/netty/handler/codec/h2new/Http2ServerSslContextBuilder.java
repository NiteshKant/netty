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
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextOption;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.internal.UnstableApi;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;

import static io.netty.util.internal.ObjectUtil.checkNonEmpty;
import static java.util.Objects.requireNonNull;

public interface Http2ServerSslContextBuilder {
    /* adds h1 for ALPN*/
    Http2ServerSslContextBuilder supportHttp1x(ChannelInitializer<Channel> http1xPipelineInitializer);

    <T> Http2ServerSslContextBuilder option(SslContextOption<T> option, T value);
    Http2ServerSslContextBuilder sslProvider(SslProvider provider);
    Http2ServerSslContextBuilder keyStoreType(String keyStoreType);
    Http2ServerSslContextBuilder sslContextProvider(Provider sslContextProvider);
    Http2ServerSslContextBuilder trustManager(File trustCertCollectionFile);
    Http2ServerSslContextBuilder trustManager(InputStream trustCertCollectionInputStream);
    Http2ServerSslContextBuilder trustManager(X509Certificate... trustCertCollection);
    Http2ServerSslContextBuilder trustManager(Iterable<? extends X509Certificate> trustCertCollection);
    Http2ServerSslContextBuilder trustManager(TrustManagerFactory trustManagerFactory);
    Http2ServerSslContextBuilder trustManager(TrustManager trustManager);
    Http2ServerSslContextBuilder keyManager(File keyCertChainFile, File keyFile);
    Http2ServerSslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream);
    Http2ServerSslContextBuilder keyManager(PrivateKey key, X509Certificate... keyCertChain);
    Http2ServerSslContextBuilder keyManager(PrivateKey key, Iterable<? extends X509Certificate> keyCertChain);
    Http2ServerSslContextBuilder keyManager(File keyCertChainFile, File keyFile, String keyPassword);
    Http2ServerSslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream,
                                 String keyPassword);
    Http2ServerSslContextBuilder keyManager(PrivateKey key, String keyPassword, X509Certificate... keyCertChain);
    Http2ServerSslContextBuilder keyManager(PrivateKey key, String keyPassword,
                                 Iterable<? extends X509Certificate> keyCertChain);
    Http2ServerSslContextBuilder keyManager(KeyManagerFactory keyManagerFactory);
    Http2ServerSslContextBuilder keyManager(KeyManager keyManager);
    Http2ServerSslContextBuilder ciphers(Iterable<String> ciphers);
    Http2ServerSslContextBuilder ciphers(Iterable<String> ciphers, CipherSuiteFilter cipherFilter);
    Http2ServerSslContextBuilder sessionCacheSize(long sessionCacheSize);
    Http2ServerSslContextBuilder sessionTimeout(long sessionTimeout);
    Http2ServerSslContextBuilder clientAuth(ClientAuth clientAuth);
    Http2ServerSslContextBuilder protocols(Iterable<String> protocols);
    Http2ServerSslContextBuilder startTls(boolean startTls);
    @UnstableApi
    Http2ServerSslContextBuilder enableOcsp(boolean enableOcsp);

    Http2ServerSslContext build() throws SSLException;
}
