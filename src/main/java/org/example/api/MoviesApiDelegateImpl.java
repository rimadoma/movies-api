package org.example.api;

import org.example.dao.MovieDao;
import org.example.model.Movie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Concrete implementation of the API interface generated from the Open API definition in
 * movies.yml
 * <p>
 * Launched by the main Spring App.
 * </p>
 */
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

    public ResponseEntity<Movie> addMovie(Movie movie) {
        final Movie addedMovie = movieDao.replaceMovie(movie);

        return ResponseEntity.ofNullable(addedMovie);
    }
}
