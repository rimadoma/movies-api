package org.example.api;

import org.example.MovieDao;
import org.example.model.Movie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO Error handling, e.g 500 in case of error in accessing persisted data
@Service
public class MoviesApiDelegateImpl implements MoviesApiDelegate {
    private final MovieDao movieDao;

    public MoviesApiDelegateImpl(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Override
    public ResponseEntity<List<Movie>> listMovies() {
        List<Movie> movies = movieDao.findAll();

        return ResponseEntity.ok(movies);
    }

    @Override
    public ResponseEntity<List<Movie>> partialNameSearch(String partialName) {
        List<Movie> movies = movieDao.findByPartialName(partialName);

        return ResponseEntity.ok(movies);
    }
}
