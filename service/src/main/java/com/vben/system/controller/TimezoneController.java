package com.vben.system.controller;

import com.vben.system.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@Tag(name = "时区接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimezoneController {

    @Operation(summary = "获取系统的时区")
    @GetMapping("/timezone/getTimezone")
    public ApiResponse<String>  getTimezone() {
      String timezone = ZoneId.systemDefault().getId();
      return ApiResponse.ok(timezone);
    }
}
