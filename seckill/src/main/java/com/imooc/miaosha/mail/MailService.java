package com.imooc.miaosha.mail;

import com.imooc.miaosha.domain.OrderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * @author wangql
 * @date 2020/4/16  14:18
 * @描述
 */
@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String mail, OrderInfo orderInfo) throws  Exception{
        //1、创建一个复杂的消息邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        //邮件设置
        helper.setSubject("秒杀成功邮件提醒");
        helper.setText("<b style='color:red'>恭喜，秒杀到了商品"+orderInfo.getId()+"</b>",true);
        helper.setTo(mail);
        helper.setFrom("1187674187@qq.com");
        mailSender.send(mimeMessage);
    }
}
