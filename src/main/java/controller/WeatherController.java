package controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.WeatherService;

@RequiredArgsConstructor
@RestController
public class WeatherController {

  private final WeatherService weatherService;

  private final String API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
  private final String SERVICE_KEY = "%2B4Prahol80blyOZ%2F4erVmmDgGmDb2KbjalyKgd9cRGOE5HvVkBLRetPwt93SXayZiPzA4Huut%2FWmCIwyfJ1mOg%3D%3D";

  @GetMapping("/weather")
  public ResponseEntity<String> getWeatherInfo(
      @RequestParam("baseDate") String baseDate,
      @RequestParam("baseTime") String baseTime,
      @RequestParam("nx") String nx,
      @RequestParam("ny") String ny
  ) {
    String url = API_URL + "?serviceKey=" + SERVICE_KEY + "&numOfRows=900&pageNo=1&dataType=JSON"
        + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny;

    String response = weatherService.processWeatherData(url);
    return ResponseEntity.ok(response);
  }
}
