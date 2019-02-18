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
		
		//����redis
		Jedis jedis=new Jedis("localhost");
		System.out.println("���ӳɹ�...");
		System.out.println("�����������У�"+jedis.ping());
		
		//�õ��û��������Ϣ����Ҫд���ļ�/�����У���AI���ж�ȡ
		String content=msg.text();
		if(content==null||content=="") {
			System.out.println("content Ϊnull");
			return ;
		}
		System.out.println("���յ�����Ϣ��"+content);

		//д�뻺����
		jedis.set("user_say", content+":user");
		
		Thread.sleep(1000);
		//��ȡAI���ص�����
		String AIsay=null;
		while(AIsay=="no"||AIsay==null) {
			//�ӻ����ж�ȡAI�ظ�������
			AIsay=jedis.get("ai_say");
			String [] arr=AIsay.split(":");
			AIsay=arr[0];
		}

		//��ȡ�������򻺴���д��
		jedis.set("ai_say", "no");
		//û��˵�����߻�û˵
		if(AIsay==null||AIsay=="") {
			System.out.println("AIsay==null||AIsay==\"\"");
			return;
		}
		System.out.println("AI˵��"+AIsay);
		
		clients.writeAndFlush(
				new TextWebSocketFrame(
						"AI_PigPig��"+LocalDateTime.now()
						+"˵��"+AIsay));
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("add...");
		clients.add(ctx.channel());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.out.println("�ͻ��˶Ͽ���channel��Ӧ�ĳ�idΪ��"
				+ctx.channel().id().asLongText());
		System.out.println("�ͻ��˶Ͽ���channel��Ӧ�Ķ�idΪ��"
				+ctx.channel().id().asShortText());
	}
 
}
