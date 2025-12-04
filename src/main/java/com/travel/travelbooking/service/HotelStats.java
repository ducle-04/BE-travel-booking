// src/main/java/com/travel/travelbooking/service/HotelStats.java
package com.travel.travelbooking.service;

public record HotelStats(
        long totalHotels,
        long activeHotels,
        long fiveStarHotels,
        long uniqueAddresses
) {}