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

import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslContextOption;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.internal.UnstableApi;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;

public interface Http2ClientSslContextBuilder {
    <T> Http2ClientSslContextBuilder option(SslContextOption<T> option, T value);
    Http2ClientSslContextBuilder sslProvider(SslProvider provider);
    Http2ClientSslContextBuilder keyStoreType(String keyStoreType);
    Http2ClientSslContextBuilder sslContextProvider(Provider sslContextProvider);
    Http2ClientSslContextBuilder trustManager(File trustCertCollectionFile);
    Http2ClientSslContextBuilder trustManager(InputStream trustCertCollectionInputStream);
    Http2ClientSslContextBuilder trustManager(X509Certificate... trustCertCollection);
    Http2ClientSslContextBuilder trustManager(Iterable<? extends X509Certificate> trustCertCollection);
    Http2ClientSslContextBuilder trustManager(TrustManagerFactory trustManagerFactory);
    Http2ClientSslContextBuilder trustManager(TrustManager trustManager);
    Http2ClientSslContextBuilder keyManager(File keyCertChainFile, File keyFile);
    Http2ClientSslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream);
    Http2ClientSslContextBuilder keyManager(PrivateKey key, X509Certificate... keyCertChain);
    Http2ClientSslContextBuilder keyManager(PrivateKey key, Iterable<? extends X509Certificate> keyCertChain);
    Http2ClientSslContextBuilder keyManager(File keyCertChainFile, File keyFile, String keyPassword);
    Http2ClientSslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream,
                                            String keyPassword);
    Http2ClientSslContextBuilder keyManager(PrivateKey key, String keyPassword, X509Certificate... keyCertChain);
    Http2ClientSslContextBuilder keyManager(PrivateKey key, String keyPassword,
                                            Iterable<? extends X509Certificate> keyCertChain);
    Http2ClientSslContextBuilder keyManager(KeyManagerFactory keyManagerFactory);
    Http2ClientSslContextBuilder keyManager(KeyManager keyManager);
    Http2ClientSslContextBuilder ciphers(Iterable<String> ciphers);
    Http2ClientSslContextBuilder ciphers(Iterable<String> ciphers, CipherSuiteFilter cipherFilter);
    Http2ClientSslContextBuilder sessionCacheSize(long sessionCacheSize);
    Http2ClientSslContextBuilder sessionTimeout(long sessionTimeout);
    Http2ClientSslContextBuilder protocols(Iterable<String> protocols);
    Http2ClientSslContextBuilder startTls(boolean startTls);
    @UnstableApi
    Http2ClientSslContextBuilder enableOcsp(boolean enableOcsp);

    Http2ClientSslContext build();
}
