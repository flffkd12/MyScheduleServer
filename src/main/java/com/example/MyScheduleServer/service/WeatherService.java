package com.example.MyScheduleServer.service;

import com.example.MyScheduleServer.dto.WeatherDto;
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

  public List<WeatherDto> processWeatherData(String url) throws MalformedURLException {
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

    List<WeatherDto> weatherDtoList = new ArrayList<>();

    WeatherDto weatherDto = new WeatherDto("0400", "30", "1", "3", "2", "4", "23");
    weatherDtoList.add(weatherDto);

    return weatherDtoList;
  }
}
