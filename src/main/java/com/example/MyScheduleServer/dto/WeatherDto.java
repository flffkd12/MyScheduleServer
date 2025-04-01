package com.example.MyScheduleServer.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class WeatherDto {

  private final String time;
  private final String rainProbability;
  private final String rainCode;
  private final String rainAmount;
  private final String snowAmount;
  private final String skyCode;
  private final String temperature;
}
