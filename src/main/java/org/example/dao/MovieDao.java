package org.example.dao;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.search.AutocompleteSearchOperator;
import com.mongodb.client.model.search.FuzzySearchOptions;
import com.mongodb.client.model.search.SearchOperator;
import com.mongodb.client.model.search.SearchPath;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.model.Movie;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.example.dao.MovieMappers.NAME;
import static org.example.dao.MovieMappers.YEAR;
import static org.example.dao.MovieMappers.toDocument;

/**
 * Persists and queries movie data (in MongoDB).
 */
@Service public class MovieDao {
    private static final FuzzySearchOptions fuzzyOptions =
            FuzzySearchOptions.fuzzySearchOptions().maxEdits(1);
    private static final ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
    private final MongoClient client;
    private final MongoCollection<Document> collection;

    public MovieDao(MongoClient client) {
        this.client = client;
        MongoDatabase database = client.getDatabase("harkka");
        collection = database.getCollection("movies");
    }

    public List<Movie> findAll() {
        return toMovies(collection.find());
    }

    public List<Movie> findByPartialName(String partialName) {
        // NB this aggregation necessitates a proper autocomplete search index in the collection to work
        final AutocompleteSearchOperator operator =
                SearchOperator.autocomplete(SearchPath.fieldPath(NAME), partialName)
                        .fuzzy(fuzzyOptions);
        Bson search = Aggregates.search(operator);
        final AggregateIterable<Document> aggregate = collection.aggregate(List.of(search));
        return toMovies(aggregate);
    }

    // Since we don't yet know how we want to handle data from API calls and uniquely identify movies the simplest thing is to just replace existing movie data by name,year. Though there's a risk some data is lost when movie is rewritten. Still the API consumer probably doesn't care if the movie already exists
    public Movie replaceMovie(Movie movie) {
        Bson filter =
                Filters.and(Filters.eq(NAME, movie.getName()), Filters.eq(YEAR, movie.getYear()));
        Document document = toDocument(movie);
        collection.replaceOne(filter, document, replaceOptions);
        return movie;
    }

    /**
     * Closes the MongoDB client and any open cursors.
     */
    public void close() {
        // NB You should close the client explicitly before exiting if your code leaves any open cursor
        if (client != null) {
            client.close();
        }
    }

    private static List<Movie> toMovies(MongoIterable<Document> iterable) {
        // Closes the cursor implicitly
        List<Document> movies = iterable.into(new ArrayList<>());
        return movies.stream().map(MovieMappers::fromDocument).toList();
    }
}
