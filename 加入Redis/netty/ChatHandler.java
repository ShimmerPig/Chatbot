package com.imooc.netty.websocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import redis.clients.jedis.Jedis;

public class ChatHandler 
	extends SimpleChannelInboundHandler<TextWebSocketFrame>{
	private static ChannelGroup clients=
			new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		System.out.println("channelRead0...");
		
		//连接redis
		Jedis jedis=new Jedis("localhost");
		System.out.println("连接成功...");
		System.out.println("服务正在运行："+jedis.ping());
		
		//得到用户输入的消息，需要写入文件/缓存中，让AI进行读取
		String content=msg.text();
		if(content==null||content=="") {
			System.out.println("content 为null");
			return ;
		}
		System.out.println("接收到的消息："+content);

		//写入缓存中
		jedis.set("user_say", content+":user");
		
		Thread.sleep(1000);
		//读取AI返回的内容
		String AIsay=null;
		while(AIsay=="no"||AIsay==null) {
			//从缓存中读取AI回复的内容
			AIsay=jedis.get("ai_say");
			String [] arr=AIsay.split(":");
			AIsay=arr[0];
		}

		//读取后马上向缓存中写入
		jedis.set("ai_say", "no");
		//没有说，或者还没说
		if(AIsay==null||AIsay=="") {
			System.out.println("AIsay==null||AIsay==\"\"");
			return;
		}
		System.out.println("AI说："+AIsay);
		
		clients.writeAndFlush(
				new TextWebSocketFrame(
						"AI_PigPig在"+LocalDateTime.now()
						+"说："+AIsay));
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("add...");
		clients.add(ctx.channel());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端断开，channel对应的长id为："
				+ctx.channel().id().asLongText());
		System.out.println("客户端断开，channel对应的短id为："
				+ctx.channel().id().asShortText());
	}
 
}
