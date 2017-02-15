package example;

import java.util.List;

import org.json.JSONObject;

import nz.paymark.oepayment.model.Payment;
import nz.paymark.settlement.service.ProxyHelper;
import nz.paymark.settlement.service.impl.ProxyHelperImpl;

public class Main {

	public static void main(String[] args) {
		ProxyHelper proxyHelper = new ProxyHelperImpl();
    	String oauthToken = proxyHelper.getAccessToken();
    	System.out.println(oauthToken);
    	JSONObject unsettledPayments = proxyHelper.getUnsettledPayments(oauthToken);
    	System.out.println(unsettledPayments);
//    	for(Payment payment : unsettledPayments) {
//    		proxyHelper.updatePayment(payment, oauthToken);
//    	}
	}
}
