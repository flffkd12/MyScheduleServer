package com.example.MyScheduleServer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherItem {

  private String baseDate;
  private String baseTime;
  private String category;
  private String fcstDate;
  private String fcstTime;
  private String fcstValue;
  private String nx;
  private String ny;
}
