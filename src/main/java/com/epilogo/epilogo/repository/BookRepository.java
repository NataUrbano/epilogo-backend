package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Author;
import com.epilogo.epilogo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByAuthor(Author author);
    List<Book> findByTitle(String title);
}
