package com.imooc.miaosha.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.mail.MailService;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.sms.SmsUtil;
import com.imooc.miaosha.vo.Sms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;

import java.util.HashMap;
import java.util.Map;

@Service
public class MQReceiver {

		private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
		
		@Autowired
		RedisService redisService;
		
		@Autowired
		GoodsService goodsService;
		
		@Autowired
		OrderService orderService;
		
		@Autowired
		MiaoshaService miaoshaService;

		@Autowired
		MailService mailService;

		@Autowired
		MiaoshaUserService miaoshaUserService;

		@Autowired
		private SmsUtil smsUtil;

		@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
		public void receive(String message) {
			log.info("receive message:"+message);
			MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
			MiaoshaUser user = mm.getUser();
			long goodsId = mm.getGoodsId();
			
			GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	    	int stock = goods.getStockCount();
	    	if(stock <= 0) {
	    		return;
	    	}
	    	//判断是否已经秒杀到了
	    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
	    	if(order != null) {
	    		return;
	    	}
	    	//减库存 下订单 写入秒杀订单
	    	miaoshaService.miaosha(user, goods);
		}
		//监听邮件对列，监听到消息就向指定的用户邮箱发送邮件提醒
		@RabbitListener(queues=MQConfig.EMAIL_QUEUE)
		public void receiveEmail(String message) throws  Exception{
			System.out.println("邮件监听");
			OrderInfo orderInfo  = RedisService.stringToBean(message, OrderInfo.class);
			long userId = orderInfo.getUserId();
			MiaoshaUser miaoshaUser = miaoshaUserService.getById(userId);
			mailService.sendMail(miaoshaUser.getMail(),orderInfo);
		}

	//监听短信对列，监听到消息就向指定的用户邮箱发送邮件提醒
	@RabbitListener(queues=MQConfig.SMS_QUEUE)
	public void receiveSms(String message) throws  Exception{
		System.out.println("短信监听");
		Sms sms  = RedisService.stringToBean(message, Sms.class);
		Map param = new HashMap();
		// getSmsCode是短信验证码。
		param.put("code",sms.getSmsCode());
		try {
			SendSmsResponse response = smsUtil.sendSms(sms.getMobile(),
					sms.getTemplateCode(),
					sms.getSignName(),
					JSON.toJSONString(param));
			System.out.println("code:"+response.getCode());
			System.out.println("message:"+response.getMessage());


		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@RabbitListener(queues = "DEAD_LETTER_QUEUE")
	public void consumeExpireOrder(String message){
		OrderInfo orderInfo  = RedisService.stringToBean(message, OrderInfo.class);
		System.out.println("in the 死信监听中");
		try {
			log.info("用户秒杀成功后超时未支付-监听者-接收消息:{}",orderInfo);
			if (orderInfo!=null && orderInfo.getStatus().intValue()==0){
				orderService.updateStatus(orderInfo.getId());
				long goodsId = orderInfo.getGoodsId();
				//修改库存
				long stock = redisService.incr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
			}
		}catch (Exception e){
			log.error("用户秒杀成功后超时未支付-监听者-发生异常：",e.fillInStackTrace());
		}
	}


//		@RabbitListener(queues=MQConfig.QUEUE)
//		public void receive(String message) {
//			log.info("receive message:"+message);
//		}
//		
//		@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
//		public void receiveTopic1(String message) {
//			log.info(" topic  queue1 message:"+message);
//		}
//		
//		@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
//		public void receiveTopic2(String message) {
//			log.info(" topic  queue2 message:"+message);
//		}
//		
//		@RabbitListener(queues=MQConfig.HEADER_QUEUE)
//		public void receiveHeaderQueue(byte[] message) {
//			log.info(" header  queue message:"+new String(message));
//		}
//		
		
}
