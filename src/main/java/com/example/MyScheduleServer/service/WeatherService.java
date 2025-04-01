package com.example.MyScheduleServer.service;

import com.example.MyScheduleServer.dto.WeatherDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

  public List<WeatherDto> processWeatherData(String url) {
    List<WeatherDto> weatherDtoList = new ArrayList<>();

    WeatherDto weatherDto = new WeatherDto("0400", "30", "1", "3", "2", "4", "23");
    weatherDtoList.add(weatherDto);

    return weatherDtoList;
  }
}
