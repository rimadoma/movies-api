package org.example;

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
import org.example.model.Personnel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles MongoDB operations and transforms the documents into domain models.
 */
// TODO Consider refactoring mappers into a separate class
@Service public class MovieDao {
    private static final String NAME = "name";
    private static final FuzzySearchOptions fuzzyOptions =
            FuzzySearchOptions.fuzzySearchOptions().maxEdits(1);
    private static final ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
    public static final String YEAR = "year";
    public static final String DIRECTOR = "director";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String AGE_LIMIT = "ageLimit";
    public static final String RATING = "rating";
    public static final String SYNOPSIS = "synopsis";
    public static final String ACTORS = "actors";
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
        List<Document> movies = new ArrayList<>();
        // Closes the cursor implicitly
        iterable.into(movies);
        return movies.stream().map(MovieDao::fromDocument).toList();
    }

    private static Movie fromDocument(Document document) {
        final Movie movie = new Movie();

        movie.setName(document.getString(NAME));
        movie.setYear(document.getInteger(YEAR));
        movie.setAgeLimit(document.getInteger(AGE_LIMIT));
        movie.setRating(document.getInteger(RATING));
        movie.setSynopsis(document.getString(SYNOPSIS));

        if (document.get(DIRECTOR, Document.class) != null) {
            final Personnel director = fromSubDocument(document.get(DIRECTOR, Document.class));
            movie.setDirector(director);
        }

        List<Document> actorDocs = document.getList(ACTORS, Document.class);
        if (actorDocs != null) {
            List<Personnel> actors = actorDocs.stream().map(MovieDao::fromSubDocument).toList();
            movie.setActors(actors);
        }

        return movie;
    }

    private static Personnel fromSubDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        Personnel p = new Personnel();
        p.setFirstName(doc.getString(FIRST_NAME));
        p.setLastName(doc.getString(LAST_NAME));
        return p;
    }


    public static Document toDocument(Movie movie) {
        Document doc = new Document();

        doc.append(NAME, movie.getName());
        doc.append(YEAR, movie.getYear());
        doc.append(AGE_LIMIT, movie.getAgeLimit());
        doc.append(RATING, movie.getRating());
        doc.append(SYNOPSIS, movie.getSynopsis());

        if (movie.getDirector() != null) {
            Document directorDoc =
                    new Document().append(FIRST_NAME, movie.getDirector().getFirstName())
                            .append(LAST_NAME, movie.getDirector().getLastName());
            doc.append(DIRECTOR, directorDoc);
        }

        if (movie.getActors() != null) {
            List<Document> actorDocs = movie.getActors().stream()
                    .map(a -> new Document().append(FIRST_NAME, a.getFirstName())
                            .append(LAST_NAME, a.getLastName())).toList();
            doc.append(ACTORS, actorDocs);
        }

        return doc;
    }
}
