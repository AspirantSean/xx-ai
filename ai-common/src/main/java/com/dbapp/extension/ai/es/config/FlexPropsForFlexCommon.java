package com.dbapp.extension.ai.es.config;

import com.dbapp.boot.core.auth.props.FlexProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Data
@Component
public class FlexPropsForFlexCommon implements FlexProperties {

	@Value(value = "${flexApiKey: null}")
	private String flexApiKey;

	@Override
	public String getApiKey() {
		return flexApiKey;
	}
}