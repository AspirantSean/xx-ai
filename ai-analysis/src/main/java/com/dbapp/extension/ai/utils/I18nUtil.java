package com.dbapp.extension.ai.utils;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * @author yangtao.ye
 * @since 2024/6/3 10:00
 */
@Slf4j
@Component
public class I18nUtil {
	@Value("${ailpha.product.lang}")
	private String productLang;

	@PostConstruct
	public void init() {
		CURR_LOCAL = StringUtils.parseLocale(productLang);
	}

	public static Locale CURR_LOCAL = Locale.US;

	private static MessageSource messageSource = SpringUtil.getBean(MessageSource.class);


	public static String getMessage(String code, Object... args) {
		return getMessage(code, CURR_LOCAL, args);
	}

	public static String getMessage(String code, Locale locale, Object... args) {
		try {
			return I18nUtil.messageSource.getMessage(code, args, locale);
		} catch (Exception e) {
			log.error("No message found！", e);
		}
		return code;
	}
}
