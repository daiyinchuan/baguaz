/**
 * Copyright (c) 2015-2016, Yinchuan Dai 戴银川 (daiyinchuan@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baguaz.util;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.jfinal.ext.plugin.artemis.ArtemisKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class EmailSender {
	private static final Logger log=LoggerFactory
			.getLogger(EmailSender.class);
	
    public static final String EMAIL_BODY_HEADER = "";
    // 邮箱服务器
    private String host;
    private String MAIL_SUBJECT = "测试邮件";
    private String sender;
    private String username;
    private String password;
    private String mail_from;
    private String charset = "utf-8";

    private static EmailSender emailSender;

    public static EmailSender getInstance() {
        if(emailSender == null) {
            emailSender = new EmailSender();
        }
        return emailSender;
    }

    public EmailSender() {
        this.host = BgzKit.getBgzProp("email.smtp");
        this.username = BgzKit.getBgzProp("email.username");
        this.password = BgzKit.getBgzProp("email.password");
        this.mail_from = BgzKit.getBgzProp("email.mailfrom");
        this.sender = BgzKit.getBgzProp("email.sendername");
    }

    /**
     * 此段代码用来发送普通电子邮件
     */
    public void send(String subject, String[] mailTo, String mailBody) throws Exception {
        try {
            Properties props = new Properties(); // 获取系统环境
            Authenticator auth = new Email_Autherticator(); // 进行邮件服务器用户认证
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.auth", "true");
            Session session = Session.getDefaultInstance(props, auth);
            // 设置session,和邮件服务器进行通讯。
            MimeMessage message = new MimeMessage(session);
            // message.setContent("foobar, "application/x-foobar"); // 设置邮件格式
            message.setSubject(subject == null ? MAIL_SUBJECT : subject, charset); // 设置邮件主题
            message.setText("<html><head><meta charset='utf-8'></head><body>" + mailBody + "</body></html>", charset, "html"); // 设置邮件正文
//          message.setHeader(mail_head_name, mail_head_value); // 设置邮件标题
            message.setSentDate(new Date()); // 设置邮件发送日期
            Address address = new InternetAddress(mail_from, sender);
            message.setFrom(address); // 设置邮件发送者的地址
            Address toAddress = null;
            for (int i = 0; i < mailTo.length; i++) {
                toAddress = new InternetAddress(mailTo[i]); // 设置邮件接收方的地址
                message.addRecipient(Message.RecipientType.TO, toAddress);
            }
            toAddress = null;
            Transport.send(message); // 发送邮件
            log.debug("邮件发送成功！");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * 用来进行服务器对用户的认证
     */
    public class Email_Autherticator extends Authenticator {
        public Email_Autherticator() {
            super();
        }

        public Email_Autherticator(String user, String pwd) {
            super();
            username = user;
            password = pwd;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    public static void syncSendMail(String title, String[] mailTo, String content) {
        String mailBody = EMAIL_BODY_HEADER + content;
        try {
            EmailSender.getInstance().send(title, mailTo, mailBody);
        } catch (Exception e) {
        	log.error("email send error:" + mailBody);
            e.printStackTrace();
        }
    }
    
    public static void asyncSendMail(String title, String[] mailTo, String content){
    	javax.jms.Session session=null;
    	try{
	    	session=ArtemisKit.getPooledSession();
	    	MessageProducer producer=session.createProducer(ArtemisKit.getQueue(ArtemisKit.QUEUE_EMAIL));
	    	ObjectMessage msg=session.createObjectMessage(new Email()
					.setTitle(title)
					.setMailTo(mailTo)
					.setContent(content));
	    	producer.send(msg);
    	} catch (JMSException e) {
    		log.error("email send jms error",e);
		}finally{
    		ArtemisKit.retPooledSession(session);
    	}
    }
    
	public static class Email implements Serializable{
		private static final long serialVersionUID = -2562018977562336303L;
		private String title;
		private String[] mailTo;
		private String content;
		public String getTitle() {
			return title;
		}
		public Email setTitle(String title) {
			this.title = title;
			return this;
		}
		public String[] getMailTo() {
			return mailTo;
		}
		public Email setMailTo(String[] mailTo) {
			this.mailTo = mailTo;
			return this;
		}
		public String getContent() {
			return content;
		}
		public Email setContent(String content) {
			this.content = content;
			return this;
		}
	}
	
	public static class EmailMessageListener implements MessageListener{
		/* (non-Javadoc)
		 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
		 */
		@Override
		public void onMessage(javax.jms.Message message) {
			ObjectMessage om=(ObjectMessage)message;
			try {
				Email email = (Email)om.getObject();
				EmailSender.syncSendMail(email.getTitle(),email.getMailTo(), email.getContent());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
