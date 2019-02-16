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

public class ChatHandler 
	extends SimpleChannelInboundHandler<TextWebSocketFrame>{
	//要写入的文件路径
	private String writeFilePath="C:\\Users\\lenovo\\Chatbot\\temp.txt";
	private String readFilePath="C:\\Users\\lenovo\\Chatbot\\temp1.txt";
	
	private static ChannelGroup clients=
			new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	//从文件中读取数据
	private static String readFromFile(String filePath) {
		File file=new File(filePath);
		String line=null;
		String name=null;
		String content=null;
		try {
			//以content:name的形式写入
			BufferedReader br=new BufferedReader(new FileReader(file));
			line=br.readLine();
			String [] arr=line.split(":");
			if(arr.length==1) {
				name=null;
				content=null;
			}else {
				content=arr[0];
				name=arr[1];
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	//向文件中覆盖地写入
	private static void writeToFile(String filePath,String content) {
		File file =new File(filePath);
		try {
			FileWriter fileWriter=new FileWriter(file);
			fileWriter.write("");
			fileWriter.flush();
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		System.out.println("channelRead0");
		//得到用户输入的消息，需要写入文件/缓存中，让AI进行读取
		String content=msg.text();
		if(content==null||content=="") {
			System.out.println("content 为null");
			return ;
		}
		System.out.println("接收到的消息："+content);
		//写入
		writeToFile(writeFilePath, content+":user");
		Thread.sleep(1000);
		//读取AI返回的内容
		String AIsay=readFromFile(readFilePath);
		//读取后马上写入
		writeToFile(readFilePath,"no");
		//没有说，或者还没说
		if(AIsay==null||AIsay==""||AIsay=="no") {
			System.out.println("AIsay==null||AIsay==\"\"||AIsay==\"no\"");
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
