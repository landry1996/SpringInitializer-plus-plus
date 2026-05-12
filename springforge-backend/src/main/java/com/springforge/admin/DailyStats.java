package com.springforge.admin;

import java.time.LocalDate;

public record DailyStats(LocalDate date, long count) {}
