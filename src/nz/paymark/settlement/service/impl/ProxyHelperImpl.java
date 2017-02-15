package nz.paymark.settlement.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nz.paymark.oepayment.model.Payment;
import nz.paymark.settlement.service.ProxyHelper;

public class ProxyHelperImpl implements ProxyHelper {

    private String urlEndpoint = "https://paymark-dev.apigee.net/";

    //Move these to a properties file
    private String clientId = "53ID1FYqKGw2lxbCrfQstR2c1Xl4o8R7";
    private String clientSecret = "XdqPh2LNLZbu6kJJ";

    
    private static final Logger logger = LoggerFactory.getLogger(ProxyHelperImpl.class);


    @Override
    public String getAccessToken() {
        String url = urlEndpoint+"/bearer";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        String basicAuth = Base64.getEncoder().encodeToString((clientId+":"+clientSecret).getBytes());
        post.setHeader("Authorization", "Basic "+ basicAuth);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            logger.debug("Status code" + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200) {
                JSONObject jsonObject = convertToJsonObject(response.getEntity().getContent());
                return jsonObject.optString("access_token");
            }
        } catch (IOException e) {
            logger.error("Exception when getting oauth token from bearer service", e);
        }
        return "";
    }

    

	@Override
	public JSONObject getUnsettledPayments(String oauthToken) {
        String fromCreationTime = LocalDateTime.now().minusMonths(1).toString();
		String toCreationTime = LocalDateTime.now().toString();
		
		String url = urlEndpoint+"v1.1/transaction/oepayment?fromCreationTime="+fromCreationTime+
        		"&toCreationTime="+toCreationTime +"&status=AUTHORISED";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", "Bearer "+ oauthToken);
        get.setHeader("Content-Type", "application/vnd.paymark_api+json");
        get.setHeader("Accept", "application/vnd.paymark_api+json");
        System.out.println(url);
        try {
            HttpResponse response = client.execute(get);

            if(response.getStatusLine().getStatusCode() == 200) {
                JSONObject jsonObject = convertToJsonObject(response.getEntity().getContent());
                return jsonObject;
            }
            logger.error("Status code {}", response.getStatusLine().getStatusCode());
            logger.error("Error when getting response from bank {}", response.toString());
        } catch (IOException e) {
            logger.error("Exception when getting response from bank ", e);
            
        }
        return null;
    }

	@Override
	public boolean updatePayment(Payment payment, String oauthToken) {
        String url = urlEndpoint+"/oepaymentsettlementsim";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(url);
        put.setHeader("Authorization", "Bearer "+ oauthToken);
        put.setHeader("Content-Type", "application/vnd.paymark_api+json");
        put.setHeader("Accept", "application/vnd.paymark_api+json");
       
        try {
            logger.info("Settle payment {}", payment.getId());
            put.setEntity(new StringEntity(payment.toString()));
            HttpResponse response = client.execute(put);

            if(response.getStatusLine().getStatusCode() != 200) {
                logger.error("Status code {}", response.getStatusLine().getStatusCode());
                logger.error("Error when updating settlementId and actualSettlementDate for payment id {} ", payment.getId() + response.toString());
            } else {
                logger.info("Update to payment id {} successful", payment.getId());
                return true;
            }
        } catch (IOException e) {
            logger.error("Error when updating settlementId and actualSettlementDate for payment id {} ", payment.getId() , e);
        }
        return false;
    }
	
	
  

    public boolean updateTransactionStatus(Integer bankPaymentRequestId, JSONObject bankResponse, String oauthToken) {
        String url = urlEndpoint+"/phauthorisationcallback";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(url);
        put.setHeader("Authorization", "Bearer "+ oauthToken);
        put.setHeader("Content-Type", "application/vnd.paymark_api+json");
        put.setHeader("Accept", "application/vnd.paymark_api+json");

        try {
            JSONObject obj=new JSONObject();
            obj.put("authorisationId", bankPaymentRequestId.toString());
            obj.put("status", bankResponse.getString("status"));
            obj.put("bankPaymentId", bankResponse.getString("bankPaymentId"));

            put.setEntity(new StringEntity(obj.toString()));

            logger.info("Updating status of bankPaymentRequestId {} to status {}", bankPaymentRequestId, bankResponse.getString("status"));
            HttpResponse response = client.execute(put);

            if(response.getStatusLine().getStatusCode() != 200) {
                logger.error("Status code {}", response.getStatusLine().getStatusCode());
                logger.error("Error when updating status for bankPaymentRequestId {} ", bankPaymentRequestId + response.toString());
            } else {
                logger.info("Update to bankPaymentRequestId {} successful", bankPaymentRequestId);
                return true;
            }
        } catch (IOException e) {
            logger.error("Error when updating status for bankPaymentRequestId {} ", bankPaymentRequestId , e);
        }
        return false;
    }

    private static JSONObject convertToJsonObject(InputStream content) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(content));

        StringBuffer jsonData  = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            jsonData .append(line);
        }
        JSONObject jsonObject = new JSONObject(jsonData.toString());
        return jsonObject;
    }


}
