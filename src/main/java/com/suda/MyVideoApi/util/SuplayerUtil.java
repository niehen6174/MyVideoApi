package com.suda.MyVideoApi.util;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guhaibo
 * @date 2018/8/5
 */
public class SuplayerUtil {

    /**
     * 解析视频播放地址
     *
     * @param refererUrl
     * @param playerUrl
     * @param api
     * @return
     */
    public static String getPlayUrl(String refererUrl, String playerUrl, String api) {
        try {
            Document pcDocument = JsoupUtils
                    .getConnection(playerUrl)
                    .header("Referer", refererUrl)
                    .get();

            Elements script = pcDocument.body().getElementsByTag("script");
            String scriptText = script.get(0).data();
            Pattern pattern = Pattern.compile(" = '(.+?)'");
            Matcher m = pattern.matcher(scriptText);

            List<String> params = new ArrayList<>();
            while (m.find()) {
                params.add(m.group().replace("= '", "").replace("'", "").trim());
            }

            Map<String, String> map = new HashMap<>();
            map.put("type", params.get(3));
            map.put("vkey", params.get(4));
            map.put("ckey", params.get(2));
            map.put("userID", "");
            map.put("userIP", params.get(0));
            map.put("refres", "1");
            map.put("my_url", params.get(1));

            int tryTime = 5;
            String playUrl = null;
            while (tryTime > 0 && StringUtils.isEmpty(playUrl)) {
                Document document = JsoupUtils
                        .getConnection(api)
                        .header("Referer", playerUrl)
                        .data(map)
                        .post();
                try {
                    playUrl = JSONObject.parseObject(document.body().text()).getString("url");
                } catch (Exception e) {
                }
                tryTime--;
            }
            return playUrl;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
