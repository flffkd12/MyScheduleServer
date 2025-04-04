package com.example.MyScheduleServer.controller;

import com.example.MyScheduleServer.dto.WeatherItem;
import com.example.MyScheduleServer.service.WeatherService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
      e.printStackTrace();
      return ResponseEntity.badRequest().body(Map.of("message", "Invalid baseDate or baseTime. "
          + "Query parameter baseDate must be yyyyMMdd like 20240728 and baseTime must be HHmm like 0500"));
    }

    final String API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    final String SERVICE_KEY = "%2B4Prahol80blyOZ%2F4erVmmDgGmDb2KbjalyKgd9cRGOE5HvVkBLRetPwt93SXayZiPzA4Huut%2FWmCIwyfJ1mOg%3D%3D";
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
      System.out.println("Error occurred assigning URL object in processWeatherDate method.");
      System.out.println("Solution: Check if the URL format is right.");

      return ResponseEntity.internalServerError().body(
          Map.of("message", "Failed to get data because of invalid URL."));
    } catch (ProtocolException e) {
      e.printStackTrace();
      System.out.println(
          "Error occurred at setRequestMethod() in processWeatherData method.");
      System.out.println("Solution1: Check if the HTTP version matches with API portal.");
      System.out.println("Solution2: Verify if used HTTP method is supported by the API portal.");

      return ResponseEntity.internalServerError()
          .body(Map.of("message", "Failed to get data because of protocol issue."));
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error occurred during IO operations.");
      System.out.println("Solution1: Check if network connection is available.");
      System.out.println("Solution2: Check if the service key has expired.");
      System.out.println("Solution3: Check if request API URL has changed.");

      return ResponseEntity.internalServerError()
          .body(Map.of("message", "Failed to get data because of IO issues."));
    }
  }
}
