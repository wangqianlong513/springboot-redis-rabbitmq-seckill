package com.imooc.miaosha.vo;

import com.imooc.miaosha.domain.OrderInfo;

/**
 * @author wangql
 * @date 2020/5/5  16:15
 * @描述 发送短信验证的时候，把此类对象发送到队列中
 */

public class Sms {
    // 手机号
    private String mobile;
    // 随机生成的验证码
    private String smsCode;
    // 订阅阿里大于短信服务时的模板名称
    private String templateCode;
    // 订阅阿里大于短信服务时的签名
    private String signName;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public Sms(String mobile, String smsCode, String templateCode, String signName) {
        this.mobile = mobile;
        this.smsCode = smsCode;
        this.templateCode = templateCode;
        this.signName = signName;;
    }
}
