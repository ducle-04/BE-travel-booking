package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.BookingContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingContactRepository extends JpaRepository<BookingContact, Long> {
}