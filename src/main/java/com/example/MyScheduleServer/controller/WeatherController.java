package com.example.MyScheduleServer.controller;

import com.example.MyScheduleServer.dto.WeatherDto;
import com.example.MyScheduleServer.service.WeatherService;
import java.net.MalformedURLException;
import java.util.List;
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
  public ResponseEntity<List<WeatherDto>> getWeatherInfo(
      @RequestParam("baseDate") String baseDate,
      @RequestParam("baseTime") String baseTime,
      @RequestParam("nx") String nx,
      @RequestParam("ny") String ny
  ) {
    String url = API_URL + "?serviceKey=" + SERVICE_KEY + "&numOfRows=900&pageNo=1&dataType=JSON"
        + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny;

    try {
      List<WeatherDto> response = weatherService.processWeatherData(url);
      return ResponseEntity.ok(response);
    } catch (MalformedURLException e) {
      return ResponseEntity.badRequest().body(null); // 적합하지 않은 쿼리 파라미터
    }
  }
}
