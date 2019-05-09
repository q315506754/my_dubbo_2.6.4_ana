/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher;


import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Dispatcher;
import com.alibaba.dubbo.remoting.exchange.support.header.HeartbeatHandler;
import com.alibaba.dubbo.remoting.transport.MultiMessageHandler;

public class ChannelHandlers {

    private static ChannelHandlers INSTANCE = new ChannelHandlers();

    protected ChannelHandlers() {
    }

    public static ChannelHandler wrap(ChannelHandler handler, URL url) {
        return ChannelHandlers.getInstance().wrapInternal(handler, url);
    }

    protected static ChannelHandlers getInstance() {
        return INSTANCE;
    }

    static void setTestingChannelHandlers(ChannelHandlers instance) {
        INSTANCE = instance;
    }

    protected ChannelHandler wrapInternal(ChannelHandler handler, URL url) {
        //装饰者模式
        //多消息处理器（心跳处理器（转发处理器（源处理器）））
        //多消息处理器、心跳处理器是简单的代理处理器子类
        //All转发处理器返回的是一个包装处理器，内部使用ExecutorService对处理器进行调用，统统用线程执行，其它Dispatcher返回的处理器的会判断是否有必要用线程执行
        ////值得一提的是包装者处理器初始化时会加载ThreadPool扩展点的adaptor版本，没有也没关系，内部有一个共享的无界线程池可以使用
        ////此外还有一个DataStore扩展点，用来存 组件名（比如ExecutorService.class.getName()）-端口号-executor，目前只有一个简单Map实现

        //心跳处理器用来对心跳双工请求进行回应  类HeaderExchangeHandler

        //多消息处理器则是对多条消息进行拆分处理
        return new MultiMessageHandler(new HeartbeatHandler(ExtensionLoader.getExtensionLoader(Dispatcher.class)
                .getAdaptiveExtension().dispatch(handler, url)));
    }
}
