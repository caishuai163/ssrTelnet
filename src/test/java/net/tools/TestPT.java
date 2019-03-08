package net.tools;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import vultr.FireWallRule;

public class TestPT {
    @Test
    public void testPing()
            throws JsonParseException, JsonMappingException, IOException {
        JsonResult pingResult = PTUtil.telnetResult("207.246.100.51", 34189,
            1000);
        if (pingResult.getCode().equals(Constants.ResultCode.SUCCESS.val())) {
            return;
        }
        String asString = Request.Get(
            "https://api.vultr.com/v1/firewall/rule_list?FIREWALLGROUPID=62c999b3&direction=in&ip_type=v4")
                .addHeader("API-Key", "AJRGMOP5DJY4SVFMP3LPDW6B2XQNQ6M4FSQA")
                .execute().returnContent().asString();
        System.out.println(asString);
        Map<String, FireWallRule> readValue = new ObjectMapper().readValue(
            asString, new TypeReference<Map<String, FireWallRule>>() {});
        readValue.forEach((k, v) -> {
            if (v.getPort() > 10000 && v.getPort() < 60000
                    && v.getPort() != 34187) {
                try {
                    Form delete = Form.form().add("FIREWALLGROUPID", "62c999b3")
                            .add("rulenumber", k);

                    int deleteStatus = Request
                            .Post(
                                "https://api.vultr.com/v1/firewall/rule_delete")
                            .addHeader("API-Key",
                                "AJRGMOP5DJY4SVFMP3LPDW6B2XQNQ6M4FSQA")
                            .bodyForm(delete.build()).execute().returnResponse()
                            .getStatusLine().getStatusCode();
                    System.out.println(deleteStatus);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

        });
        Form add = Form.form().add("FIREWALLGROUPID", "62c999b3")
                .add("direction", "in").add("ip_type", "v4")
                .add("subnet", "0.0.0.0").add("subnet_size", "0")
                .add("port", "34189").add("notes", "ssr");
        HttpResponse response = Request
                .Post("https://api.vultr.com/v1/firewall/rule_create")
                .addHeader("API-Key", "AJRGMOP5DJY4SVFMP3LPDW6B2XQNQ6M4FSQA")
                .bodyForm(add.add("protocol", "tcp").build()).execute()
                .returnResponse();
        String data = EntityUtils.toString(response.getEntity());
        System.out.println(data);
        HttpResponse response2 = Request
                .Post("https://api.vultr.com/v1/firewall/rule_create")
                .addHeader("API-Key", "AJRGMOP5DJY4SVFMP3LPDW6B2XQNQ6M4FSQA")
                .bodyForm(add.add("protocol", "udp").build()).execute()
                .returnResponse();
        String data2 = EntityUtils.toString(response2.getEntity());
        System.out.println(data2);
    }

}
