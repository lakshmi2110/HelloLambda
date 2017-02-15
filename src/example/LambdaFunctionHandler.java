package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<String, String> {

    @Override
    public String handleRequest(String input, Context context) {
    	
//    	ProxyHelper proxyHelper = new ProxyHelperImpl();
//    	String oauthToken = proxyHelper.getAccessToken();
//    	
//    	List<Payment> unsettledPayments = proxyHelper.getUnsettledPayments(oauthToken);
//    	
//    	for(Payment payment : unsettledPayments) {
//    		proxyHelper.updatePayment(payment, oauthToken);
//    	}
//
    	String output = "Completed execution";
        return output;
    }

}
