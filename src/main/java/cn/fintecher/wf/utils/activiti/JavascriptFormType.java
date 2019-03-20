package cn.fintecher.wf.utils.activiti;

import org.activiti.engine.form.AbstractFormType;

/**
 * Javascript表单字段
 */
public class JavascriptFormType extends AbstractFormType {

	private static final long serialVersionUID = -5607272318158162487L;

	@Override
    public String getName() {
        return "javascript";
    }

    @Override
    public Object convertFormValueToModelValue(String propertyValue) {
        return propertyValue;
    }

    @Override
    public String convertModelValueToFormValue(Object modelValue) {
        return (String) modelValue;
    }
}
