#coding=utf-8
#（4）使用训练好的模型
import tensorflow as tf  # 0.12
from tensorflow.models.tutorials.rnn.chatbot import seq2seq_model#在翻译模型中，引入seq2seq_model
import os
import numpy as np
import redis

host='localhost'
port=6379

r=redis.Redis(host=host, port=port)
 
PAD_ID = 0
GO_ID = 1
EOS_ID = 2
UNK_ID = 3
tf.reset_default_graph()
#词汇表路径path
train_encode_vocabulary = 'train_encode_vocabulary'
train_decode_vocabulary = 'train_decode_vocabulary'
 
#读取词汇表
def read_vocabulary(input_file):
    tmp_vocab = []
    with open(input_file, "r",encoding='utf-8') as f:
        tmp_vocab.extend(f.readlines())#打开的文件全部读入input_file中
    tmp_vocab = [line.strip() for line in tmp_vocab]#转换成列表
    vocab = dict([(x, y) for (y, x) in enumerate(tmp_vocab)])
    return vocab, tmp_vocab#返回字典，列表
 
 
vocab_en, _, = read_vocabulary(train_encode_vocabulary)#得到词汇字典
_, vocab_de, = read_vocabulary(train_decode_vocabulary)#得到词汇列表
 
# 词汇表大小5000
vocabulary_encode_size = 5000
vocabulary_decode_size = 5000
 
buckets = [(5, 10), (10, 15), (20, 25), (40, 50)]
layer_size = 256  # 每层大小
num_layers = 3  # 层数
batch_size = 1
 
model = seq2seq_model.Seq2SeqModel(source_vocab_size=vocabulary_encode_size, target_vocab_size=vocabulary_decode_size,
                                   buckets=buckets, size=layer_size, num_layers=num_layers, max_gradient_norm=5.0,
                                   batch_size=batch_size, learning_rate=0.5, learning_rate_decay_factor=0.99,
                                   forward_only=True)
#模型说明：源,目标词汇尺寸=vocabulary_encode(decode)_size;batch_size:训练期间使用的批次的大小;#forward_only:仅前向不传递误差
 
model.batch_size = 1#batch_size=1
 
with tf.Session() as sess:#打开作为一次会话
    # 恢复前一次训练
    ckpt = tf.train.get_checkpoint_state('.')#从检查点文件中返回一个状态(ckpt)
    #如果ckpt存在，输出模型路径
    if ckpt != None:
        print(ckpt.model_checkpoint_path)
        model.saver.restore(sess, ckpt.model_checkpoint_path)#储存模型参数
    else:
        print("没找到模型")
    r.set('user_say','no')
    #测试该模型的能力
    while True:
        line='no'
        #从缓存中进行读取
        while line=='no':
            line=r.get('user_say').decode()
            #print(line)
        list1=line.split(':')
        if len(list1)==1:
            input_string='no'
        else:
            input_string=list1[0]
            r.set('user_say','no')
		#从文件中进行读取
        #input_string = input('me > ')
        #with open('./temp.txt','r+',encoding='ANSI') as myf:
            #line=myf.read()
            #list1=line.split(':')
            #if len(list1)==1:
            #    input_string='no'
            #else:
            #    input_string=list1[0]
            #    myf.seek(0)
                #清空文件
            #    myf.truncate()
            #    myf.write('no')
                             
		
    # 退出
        if input_string == 'quit':
           exit()
        if input_string != 'no':
            input_string_vec = []#输入字符串向量化
            for words in input_string.strip():
                input_string_vec.append(vocab_en.get(words, UNK_ID))#get()函数：如果words在词表中，返回索引号；否则，返回UNK_ID
                bucket_id = min([b for b in range(len(buckets)) if buckets[b][0] > len(input_string_vec)])#保留最小的大于输入的bucket的id
                encoder_inputs, decoder_inputs, target_weights = model.get_batch({bucket_id: [(input_string_vec, [])]}, bucket_id)
                #get_batch(A,B):两个参数，A为大小为len(buckets)的元组，返回了指定bucket_id的encoder_inputs,decoder_inputs,target_weights
                _, _, output_logits = model.step(sess, encoder_inputs, decoder_inputs, target_weights, bucket_id, True)
                #得到其输出
                outputs = [int(np.argmax(logit, axis=1)) for logit in output_logits]#求得最大的预测范围列表
                if EOS_ID in outputs:#如果EOS_ID在输出内部，则输出列表为[,,,,:End]
                    outputs = outputs[:outputs.index(EOS_ID)]
             
                response = "".join([tf.compat.as_str(vocab_de[output]) for output in outputs])#转为解码词汇分别添加到回复中
                print('AI-PigPig > ' + response)#输出回复
                #向缓存中进行写入
                r.set('ai_say',response+':AI')
                #向文件中进行写入
                #with open('./temp1.txt','w',encoding='ANSI') as myf1:
                #    myf1.write(response+':AI')
                
