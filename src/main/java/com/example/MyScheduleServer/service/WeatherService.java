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

  public List<WeatherItem> getWeatherItemList(String url)
      throws MalformedURLException, ProtocolException, IOException {

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

    List<WeatherItem> weatherItemList = new ArrayList<>();
    if (items.isArray()) {
      for (JsonNode item : items) {
        WeatherItem weatherItem = objectMapper.treeToValue(item, WeatherItem.class);
        weatherItemList.add(weatherItem);
      }
    }

    return weatherItemList;
  }
}
