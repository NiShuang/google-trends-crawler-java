import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.dongliu.requests.Requests;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public String getToken(String[] keyList) {
        JSONObject req = new JSONObject();
        JSONArray comparisonItem = new JSONArray();
        for (String s : keyList) {
            JSONObject temp = new JSONObject();
            temp.put("geo", "");
            temp.put("keyword", s.replace(" ", "%20"));
            temp.put("time", "today+1-m");
            comparisonItem.add(temp);
        }
        req.put("comparisonItem", comparisonItem);
        req.put("category", 0);
        req.put("property", "");
        Map<String, String> params = new HashMap<>();
        params.put("hl", "zh-CN");
        params.put("tz", "-480");
        params.put("req", req.toJSONString().replace(" ", "").replace("\"", "'"));
        String url = "https://trends.google.com/trends/api/explore?";
        for (String s : params.keySet()) {
            url += s + "=" + params.get(s) + "&";
        }
        String response = Requests.get(url).headers(getHeaders(keyList)).send().readToText();
        response = response.substring(5);
        JSONObject jsonObject = JSON.parseObject(response);
        return jsonObject.getJSONArray("widgets").getJSONObject(0).getString("token");
    }

    public String getTrend(String[] keyList, String startDate, String endDate) {
        String token = getToken(keyList);

        JSONObject req = new JSONObject();

        JSONArray comparisonItem = new JSONArray();
        for (String s : keyList) {
            JSONObject temp = new JSONObject();
            JSONObject complexKeywordsRestriction = new JSONObject();
            JSONArray keyword = new JSONArray();
            JSONObject keywordJson = new JSONObject();
            keywordJson.put("type", "BROAD");
            keywordJson.put("value", s.replace(" ", "%20"));
            keyword.add(keywordJson);
            complexKeywordsRestriction.put("keyword", keyword);
            temp.put("complexKeywordsRestriction", complexKeywordsRestriction);
            temp.put("geo", new JSONObject());
            comparisonItem.add(temp);
        }

        JSONObject requestOptions = new JSONObject();
        requestOptions.put("property", "");
        requestOptions.put("backend", "IZG");
        requestOptions.put("category", 0);

        req.put("requestOptions", requestOptions);
        req.put("comparisonItem", comparisonItem);
        req.put("resolution", "DAY");
        req.put("locale", "zh-CN");
        req.put("time", startDate + " +" + endDate);

        Map<String, String> params = new HashMap<>();
        params.put("hl", "zh-CN");
        params.put("tz", "-480");
        params.put("req", req.toJSONString().replace(" ", "").replace("\"", "'"));
        params.put("token", token);
        String url = "https://trends.google.com/trends/api/widgetdata/multiline?";
        for (String s : params.keySet()) {
            url += s + "=" + params.get(s) + "&";
        }
        System.out.println(url);
        String response = Requests.get(url).headers(getHeaders(keyList)).send().readToText();
        return response.substring(5);
    }

    public Map<String, String> getHeaders(String[] keyList) {
        String q =  String.join(",", keyList);
        String encodeQ = q;
        try {
            encodeQ = URLEncoder.encode(q, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "trends.google.com");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0");
        headers.put("Referfer", "https://trends.google.com/trends/explore?date=today%201-m&q=" + encodeQ.replace("+", "%20"));
        headers.put("Cookie", "__utmt=1; __utma=10102256.539038748.1495043708.1495043708.1495435554.2; __utmb=10102256.8.9.1495435587029; __utmc=10102256; __utmz=10102256.1495043708.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); NID=103=JBmZSCUdgzRzy0ZMp31uy5nS1gwKm-imoboVE3nf2HrEX-UXQO95jS1NNaaFE1bkUkQ5MQkc-lveM2g3h4evgY12Bs4UpJS4PbUXBuwiM7CkqAwo8TfRrVPa-wH7uieP");
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("x-client-data", "CJG2yQEIpbbJAQjEtskBCPucygEIqZ3KAQ==");

        return headers;
    }



    public static void main(String[] args) {
        Main m = new Main();
        String[] keyList = new String[]{"insta360", "samsung gear 360", "theta s", "Giroptic", "GoPro Fusion"};
        Calendar today = Calendar.getInstance();
        int month = (today.get(Calendar.MONTH) + 10) % 12 +1;
        int year = today.get(Calendar.YEAR) - month / 12;
        Calendar before = Calendar.getInstance();
        before.set(Calendar.YEAR, year);
        before.set(Calendar.MONTH, month);
        int day = Math.min(today.get(Calendar.DAY_OF_MONTH), before.getActualMaximum(Calendar.DAY_OF_MONTH));
        before.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置你想要的格式


        System.out.println(m.getTrend(keyList, df.format(before.getTime()), df.format(today.getTime())));
    }
}
