package cn.fintecher.wf.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

@ControllerAdvice(basePackages = "org.activiti.rest.diagram.services")
public class JsonpAdvice extends AbstractJsonpResponseBodyAdvice {

	public JsonpAdvice() {
		super("callback", "jsonp");
	}
}