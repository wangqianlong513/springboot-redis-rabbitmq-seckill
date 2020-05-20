package com.imooc.miaosha.rabbitmq;

import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.vo.Sms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.redis.RedisService;

@Service
public class MQSender {

	private static Logger log = LoggerFactory.getLogger(MQSender.class);
	
	@Autowired
	RabbitTemplate rabbitTemplate ;

	@Value("${template_code}")
	private String template_code;

	@Value("${sign_name}")
	private String sign_name;

	public void sendMiaoshaMessage(MiaoshaMessage mm) {
		String msg = RedisService.beanToString(mm);
		log.info("send message:"+msg);
		rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
	}

	// 下单后发送邮件
	public void sendEmai(OrderInfo orderInfo){
		String msg = RedisService.beanToString(orderInfo);
		rabbitTemplate.convertAndSend(MQConfig.EMAIL_EXCHANGE,"",msg);
	}

	// 下单后发送短信
	public void sendSms(OrderInfo orderInfo){
		//1.生成一个6位随机数（验证码）
		final String smsCode=  (long)(Math.random()*1000000)+"";
		//final String smsCode = "rabbit";
		System.out.println("验证码："+smsCode);
		Sms sms =new Sms("18298029187",smsCode,template_code,sign_name);
		String msg = RedisService.beanToString(sms);
		rabbitTemplate.convertAndSend(MQConfig.SMS_EXCHANGE,"",msg);
	}

	// 下订单的时候，向普通队列中发送此消息
	public void sendOrderMessage(final OrderInfo orderInfo){
		String msg = RedisService.beanToString(orderInfo);
		try {
			if (orderInfo!=null){
				// 因为普通交换机是NORMAL_EXCHANGE，且普通交换机和正常队列的绑定键是NORMAL_KEY
				// 所以此处发送的消息会被路由到普通队列
				rabbitTemplate.convertAndSend("NORMAL_EXCHANGE","NORMAL_KEY",msg, new MessagePostProcessor() {
					@Override
					public Message postProcessMessage(Message message) throws AmqpException {
						MessageProperties mp=message.getMessageProperties();
						//TODO：动态设置TTL(为了测试方便，暂且设置10s)
						mp.setExpiration(String.valueOf(1000*20));
						System.out.println("in the sendOrderMessage hs");
						return message;
					}
				});
			}
		}catch (Exception e){
			log.error("秒杀成功后生成抢购订单-发送信息入死信队列，等待着一定时间失效超时未支付的订单-发生异常，消息为：{}",orderInfo.getId(),e.fillInStackTrace());
		}
	}

}
