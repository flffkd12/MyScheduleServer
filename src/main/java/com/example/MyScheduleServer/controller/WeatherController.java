package com.example.MyScheduleServer.controller;

import com.example.MyScheduleServer.dto.WeatherItem;
import com.example.MyScheduleServer.service.WeatherService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WeatherController {

  private final WeatherService weatherService;

  private final String API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
  private final String SERVICE_KEY = "%2B4Prahol80blyOZ%2F4erVmmDgGmDb2KbjalyKgd9cRGOE5HvVkBLRetPwt93SXayZiPzA4Huut%2FWmCIwyfJ1mOg%3D%3D";

  @GetMapping("/weather")
  public ResponseEntity<Map<String, Object>> getWeatherInfo(
      @RequestParam("baseDate") String baseDate,
      @RequestParam("baseTime") String baseTime,
      @RequestParam("nx") String nx,
      @RequestParam("ny") String ny
  ) {
    List<String> validBaseTime = List.of("0200", "0500", "0800", "1100", "1400", "1700", "2000",
        "2300");
    if (!validBaseTime.contains(baseTime)) {
      return ResponseEntity.badRequest()
          .body(Map.of("message",
              "Invalid baseTime query parameter. baseTime must be one of: " + Arrays.toString(
                  validBaseTime.toArray())));
    }

    try {
      LocalDateTime requestedDateTime = LocalDateTime.parse(baseDate + baseTime,
          DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
      LocalDateTime now = LocalDateTime.now();
      if (requestedDateTime.isAfter(now.minusMinutes(10)) || requestedDateTime.isBefore(
          now.minusDays(1))) {
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid baseDate or baseTime. "
            + "Weather information is available 10 minutes after the requested time, which must be within 24 hours past."));
      }
    } catch (DateTimeParseException e) {
      // parse
    } catch (IllegalArgumentException e) {
      // ofPattern
    } catch (DateTimeException e) {
      //10분 빼는 검사  /// 주요 excetion에 대해서 최대 2,3개 쓰고 나머지는 포괄적인 Exception으로 잡자 for 가동성
    }

    String url = API_URL + "?serviceKey=" + SERVICE_KEY + "&numOfRows=900&pageNo=1&dataType=JSON"
        + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny;

    try {
      List<WeatherItem> response = weatherService.processWeatherData(url);
      if (response.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("message",
            "Can't load weather data for this location. Check nx, ny query parameter for valid location."));
      } else {
        // 데이터가 존재하니 데이터에 대한 가공
        return ResponseEntity.ok(Map.of("data", response));
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body(
          Map.of("message", "Invalid parameter. Check the query parameter format."));
    } catch (ProtocolException e) {
      e.printStackTrace();
      // 클라이언트와 서버간 프로토콜 버전 일치 확인
      // 공공데이터 포탈은 GET만 수신하는데 다른거 썼는지 확인
    } catch (IOException e) {
      e.printStackTrace();
      // 네트워크 오류, 시간 초과, 서비스 키 만료, API URL변경
    }
  }
}
