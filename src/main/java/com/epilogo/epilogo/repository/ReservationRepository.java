package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Book;
import com.epilogo.epilogo.model.Reservation;
import com.epilogo.epilogo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByBook(Book book);
}
