package com.imooc.miaosha.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MQConfig {
	@Autowired
	private CachingConnectionFactory connectionFactory;
	@Autowired
	private Environment env;
	@Autowired
	private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

	public static final String MIAOSHA_QUEUE = "miaosha.queue";
	public static final String QUEUE = "queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String HEADER_QUEUE = "header.queue";
	public static final String TOPIC_EXCHANGE = "topicExchage";
	public static final String FANOUT_EXCHANGE = "fanoutxchage";
	public static final String HEADERS_EXCHANGE = "headersExchage";

	// 邮件队列
	public static final String EMAIL_QUEUE = "emailQueue";
	// 邮件交换机
	public static final String EMAIL_EXCHANGE = "emailExchange";

	// 短信队列
	public static final String SMS_QUEUE = "smsQueue";
	// 短信交换机
	public static final String SMS_EXCHANGE = "smsExchange";

	/**
	 * 死信交换机（在定义产生死信消息的队列时使用此参数），固定写法，不可改变
	 */
	private static final String DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	/**
	 * 死信交换机绑定（在定义产生死信消息的队列时使用此参数），固定写法，不可改变
	 */
	private static final String  DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

	/**
	 * Direct模式 交换机Exchange
	 * */
	@Bean
	public Queue queue() {
		return new Queue(MIAOSHA_QUEUE, true);
	}

	//定义发送邮件的队列
	@Bean
	public Queue emailQueue(){
		return new Queue(EMAIL_QUEUE,true);
	}
	// 定义fanout模式的邮件交换机
	@Bean
	public FanoutExchange emailExchange(){
		return new FanoutExchange(EMAIL_EXCHANGE);
	}
	// 绑定邮件队列和邮件交换机
	@Bean
	public Binding emailBingding(){
		return BindingBuilder.bind(emailQueue()).to(emailExchange());
	}


	//定义发送短信的队列
	@Bean
	public Queue smsQueue(){
		return new Queue(SMS_QUEUE,true);
	}
	// 定义fanout模式的短信交换机
	@Bean
	public FanoutExchange smsExchange(){
		return new FanoutExchange(SMS_EXCHANGE);
	}
	// 绑定短信队列和短信交换机
	@Bean
	public Binding smsBingding(){
		return BindingBuilder.bind(smsQueue()).to(smsExchange());
	}

	// 接下来处理超时未支付的订单，取消订单，库存修改

	// 普通队列（由于此队列可能产生死信消息，所以使用DEAD_LETTER_EXCHANGE和DEAD_LETTER_EXCHANGE参数
	// DEAD_LETTER_EXCHANGE: 当此普通队列中存在死信时，把死信转发的死信交换机
	// DEAD_LETTER_EXCHANGE：死信被转发到死信交换机后，将按照此路由键转发到死信队列）
	@Bean
	public Queue normalQueue() {
		Map<String, Object> args = new HashMap<String, Object>(2);
		args.put(DEAD_LETTER_EXCHANGE, "DEAD_LETTER_EXCHANGE");
		args.put(DEAD_LETTER_ROUTING_KEY, "REDIRECT_KEY");
		// 当产生死信队列时，把死信转发到DEAD_LETTER_EXCHANGE交换机，并通过路由REDIRECT_KEY转发到死信队列
		return new Queue("NORMAL_QUEUE", true, false, false, args);
	}
	// 死信队列
	@Bean
	public Queue deadLetterQueue() {
		return new Queue("DEAD_LETTER_QUEUE", true);
	}
	// 普通交换机
	@Bean
	public Exchange normalExchange() {
		//return ExchangeBuilder.directExchange("NORMAL_EXCHANGE").durable(true).build();
		return new DirectExchange("NORMAL_EXCHANGE");
	}
	// 死信交换机
	@Bean
	public TopicExchange deadLetterExchange() {
		return new TopicExchange("DEAD_LETTER_EXCHANGE");
	}
	// 普通队列与普通交换机绑定
	@Bean
	public Binding deadLetterBinding() {
		return new Binding(
				"NORMAL_QUEUE",
				Binding.DestinationType.QUEUE,
				"NORMAL_EXCHANGE",
				"NORMAL_KEY", null);
	}
	// 死信队列与死信交换机绑定
	@Bean
	public Binding redirectBinding() {
		return new Binding(
				"DEAD_LETTER_QUEUE",
				Binding.DestinationType.QUEUE,
				"DEAD_LETTER_EXCHANGE",
				"REDIRECT_KEY", null);
	}

	/**
	 * Topic模式 交换机Exchange
	 * */
	@Bean
	public Queue topicQueue1() {
		return new Queue(TOPIC_QUEUE1, true);
	}
	@Bean
	public Queue topicQueue2() {
		return new Queue(TOPIC_QUEUE2, true);
	}
	@Bean
	public TopicExchange topicExchage(){
		return new TopicExchange(TOPIC_EXCHANGE);
	}
	@Bean
	public Binding topicBinding1() {
		return BindingBuilder.bind(topicQueue1()).to(topicExchage()).with("topic.key1");
	}
	@Bean
	public Binding topicBinding2() {
		return BindingBuilder.bind(topicQueue2()).to(topicExchage()).with("topic.#");
	}
	/**
	 * Fanout模式 交换机Exchange
	 * */
	/*@Bean
	public FanoutExchange fanoutExchage(){
		return new FanoutExchange(FANOUT_EXCHANGE);
	}
	@Bean
	public Binding FanoutBinding1() {
		return BindingBuilder.bind(topicQueue1()).to(fanoutExchage());
	}
	@Bean
	public Binding FanoutBinding2() {
		return BindingBuilder.bind(topicQueue2()).to(fanoutExchage());
	}
	*/
	/**
	 * Header模式 交换机Exchange
	 * */
	@Bean
	public HeadersExchange headersExchage(){
		return new HeadersExchange(HEADERS_EXCHANGE);
	}
	@Bean
	public Queue headerQueue1() {
		return new Queue(HEADER_QUEUE, true);
	}
	@Bean
	public Binding headerBinding() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("header1", "value1");
		map.put("header2", "value2");
		return BindingBuilder.bind(headerQueue1()).to(headersExchage()).whereAll(map).match();
	}

	/**
	 * 单一消费者
	 * @return
	 */
	@Bean(name = "singleListenerContainer")
	public SimpleRabbitListenerContainerFactory listenerContainer(){
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(new Jackson2JsonMessageConverter());
		factory.setConcurrentConsumers(1);
		factory.setMaxConcurrentConsumers(1);
		factory.setPrefetchCount(1);
		factory.setTxSize(1);
		return factory;
	}

	/**
	 * 多个消费者
	 * @return
	 */
	@Bean(name = "multiListenerContainer")
	public SimpleRabbitListenerContainerFactory multiListenerContainer(){
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factoryConfigurer.configure(factory,connectionFactory);
		factory.setMessageConverter(new Jackson2JsonMessageConverter());
		//确认消费模式-NONE
		factory.setAcknowledgeMode(AcknowledgeMode.NONE);
		factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.simple.concurrency",int.class));
		factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.simple.max-concurrency",int.class));
		factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.simple.prefetch",int.class));
		return factory;
	}
}
