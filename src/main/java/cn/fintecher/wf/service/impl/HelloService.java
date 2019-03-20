package cn.fintecher.wf.service.impl;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class HelloService implements JavaDelegate {  
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HelloService.class);
	
	private static int COUNT = 0;
  
    @Override  
    public void execute(DelegateExecution arg0) throws Exception {  
    	LOGGER.error("---------------------------------------------");    
    	LOGGER.error("Hello Service " + this.toString() + "Is Saying Hello To Every One !");  
    	LOGGER.error("---------------------------------------------");   
    	if(true){
    		throw new Exception("errorrrrrrr");
    	}
    } 
    
    public void hello()throws Exception {
    	LOGGER.error("---------------------------------------------");    
    	LOGGER.error("Hello Service Is Saying Hello To Every One !");  
    	LOGGER.error("---------------------------------------------");  
    	COUNT++;
    	if(true){
    		throw new Exception("hello error");
    	}
    	
    }

}
