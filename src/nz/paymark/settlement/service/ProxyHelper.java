package nz.paymark.settlement.service;

import org.json.JSONObject;

import nz.paymark.oepayment.model.Payment;

public interface ProxyHelper {

    public String getAccessToken();
    
    public JSONObject getUnsettledPayments(String oauthToken);
    
    public boolean updatePayment(Payment payment, String oauthToken);
    
}
