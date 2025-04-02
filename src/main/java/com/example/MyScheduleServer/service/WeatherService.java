package com.example.MyScheduleServer.service;

import com.example.MyScheduleServer.dto.WeatherItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

  public List<WeatherItem> processWeatherData(String url) throws MalformedURLException {
    List<WeatherItem> weatherItemList = new ArrayList<>();

    try {
      URL dataUrl = new URL(url);
      HttpURLConnection conn = (HttpURLConnection) dataUrl.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-type", "application/json");

      BufferedReader rd;
      if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      } else {
        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
      }

      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
      rd.close();
      conn.disconnect();

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(sb.toString());
      JsonNode items = jsonNode.path("response").path("body").path("items").path("item");

      if (items.isArray()) {
        for (JsonNode item : items) {
          WeatherItem weatherItem = objectMapper.treeToValue(item, WeatherItem.class);
          weatherItemList.add(weatherItem);
        }
      }
    } catch (MalformedURLException e) {
      throw e;
    } catch (ProtocolException e) {
      e.printStackTrace();
      // 클라이언트와 서버간 프로토콜 버전 일치 확인
      // 공공데이터 포탈은 GET만 수신하는데 다른거 썼는지 확인
    } catch (IOException e) {
      e.printStackTrace();
      // 네트워크 오류, 시간 초과
    }

    return weatherItemList;
  }
}
