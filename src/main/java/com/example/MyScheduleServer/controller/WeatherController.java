package com.example.MyScheduleServer.controller;

import com.example.MyScheduleServer.dto.WeatherDto;
import com.example.MyScheduleServer.dto.WeatherItem;
import com.example.MyScheduleServer.service.WeatherService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
      return ResponseEntity.badRequest().body(Map.of("message",
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
    String url = API_URL + "?serviceKey=" + SERVICE_KEY + "&numOfRows=1500&pageNo=1&dataType=JSON"
        + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny;

    try {
      List<WeatherItem> weatherItemList = weatherService.getWeatherItemList(url);
      if (weatherItemList.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("message",
            "Can't load weather data for this location. Check nx, ny query parameter for valid location."));
      } else {
        List<WeatherDto> weatherDtoList = convertToWeatherDto(weatherItemList);
        return ResponseEntity.ok(Map.of("data", weatherDtoList));
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
      System.out.println("Error occurred assigning URL object in getWeatherItemList method.");
      System.out.println("Solution: Check if the URL format is right.");

      return ResponseEntity.internalServerError()
          .body(Map.of("message", "Failed to get data because of invalid URL."));
    } catch (ProtocolException e) {
      e.printStackTrace();
      System.out.println("Error occurred at setRequestMethod() in getWeatherItemList method.");
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

  private List<WeatherDto> convertToWeatherDto(List<WeatherItem> weatherItemList) {
    List<WeatherDto> weatherDtoList = new ArrayList<>();
    List<List<WeatherItem>> chunkedWeatherItemList = chunkWeatherItemList(weatherItemList);

    for (List<WeatherItem> eachWeatherItemList : chunkedWeatherItemList) {
      WeatherDto weatherDto = new WeatherDto();

      for (WeatherItem weatherItem : eachWeatherItemList) {
        weatherDto.date = weatherItem.getFcstDate();
        weatherDto.time = weatherItem.getFcstTime();

        switch (weatherItem.getCategory()) {
          case "POP":
            weatherDto.rainProbability = weatherItem.getFcstValue();
            break;
          case "PTY":
            weatherDto.rainCode = Integer.valueOf(weatherItem.getFcstValue());
            break;
          case "PCP":
            weatherDto.rainAmount = weatherItem.getFcstValue();
            break;
          case "SNO":
            weatherDto.snowAmount = weatherItem.getFcstValue();
            break;
          case "SKY":
            weatherDto.skyCode = Integer.valueOf(weatherItem.getFcstValue());
            break;
          case "TMP":
            weatherDto.temperature = weatherItem.getFcstValue();
            break;
        }
      }

      weatherDtoList.add(weatherDto);
    }

    return weatherDtoList;
  }

  private List<List<WeatherItem>> chunkWeatherItemList(List<WeatherItem> weatherItemList) {
    List<List<WeatherItem>> chunkedWeatherItemList = new ArrayList<>();
    List<WeatherItem> tempChunkList = new ArrayList<>();

    // 현재 시각의 날씨 정보를 가진 인덱스로 이동
    int weatherItemListIndex = 0;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    LocalDateTime nowDateTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

    while (weatherItemListIndex < weatherItemList.size()) {
      WeatherItem item = weatherItemList.get(weatherItemListIndex);
      String dateTimeStr = item.getFcstDate() + item.getFcstTime();
      LocalDateTime itemDateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);

      if (!itemDateTime.isBefore(nowDateTime)) {
        System.out.println(itemDateTime);
        System.out.println(nowDateTime);
        break;
      }

      weatherItemListIndex++;
    }

    // 현재 시각 부터 모레 0시까지의 데이터 추출
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate lastDate = LocalDate.parse(weatherItemList.getFirst().getBaseDate(), dateFormatter)
        .plusDays(2);

    WeatherItem weatherItem;
    WeatherItem nextWeatherItem;

    do {
      weatherItem = weatherItemList.get(weatherItemListIndex);
      nextWeatherItem = weatherItemList.get(weatherItemListIndex + 1);

      tempChunkList.add(weatherItem);

      if (!weatherItem.getFcstTime().equals(nextWeatherItem.getFcstTime())) {
        chunkedWeatherItemList.add(new ArrayList<>(tempChunkList));
        tempChunkList.clear();
      }

      weatherItemListIndex++;
    } while (!(lastDate.equals(LocalDate.parse(weatherItem.getFcstDate(), dateFormatter))
        && weatherItem.getFcstTime().equals("0100")));

    return chunkedWeatherItemList;
  }
}
