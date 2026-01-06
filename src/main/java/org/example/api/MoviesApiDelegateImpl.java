package org.example.api;

import jakarta.validation.Valid;
import org.example.dao.MovieDao;
import org.example.model.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Concrete implementation of the API delegate interface generated from the Open API definition in
 * movies.yml
 * <p>
 * Launched by the main Spring App.
 * </p>
 */
// TODO Fix validation, e.g. should get 400 when trying to add a Movie without year. Possible with the delegate pattern?
// TODO Work out response semantics, e.g. error codes for Mongo exceptions
@RestController
@Validated
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

    @Override
    public ResponseEntity<Movie> addMovie(@Valid @RequestBody Movie movie) {
        final Movie addedMovie = movieDao.replaceMovie(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(addedMovie);
    }
}
