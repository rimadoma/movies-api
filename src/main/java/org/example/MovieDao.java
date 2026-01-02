package org.example;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
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
@Service
public class MovieDao {
    private static final String NAME = "name";
    private static final FuzzySearchOptions fuzzyOptions =
            FuzzySearchOptions.fuzzySearchOptions().maxEdits(1);
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

    private static List<Movie> toMovies(MongoIterable<Document> iterable) {
        List<Document> movies = new ArrayList<>();
        // Closes the cursor implicitly
        iterable.into(movies);
        return movies.stream().map(MovieDao::fromDocument).toList();
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

    // TODO Consider refactoring into a mapper class
    private static Movie fromDocument(Document document) {
        final Movie movie = new Movie();

        movie.setName(document.getString(NAME));
        movie.setYear(document.getInteger("year"));
        movie.setAgeLimit(document.getInteger("ageLimit"));
        movie.setRating(document.getInteger("rating"));
        movie.setSynopsis(document.getString("synopsis"));

        if (document.get("director", Document.class) != null) {
            final Personnel director = fromSubDocument(document.get("director", Document.class));
            movie.setDirector(director);
        }

        List<Document> actorDocs = document.getList("actors", Document.class);
        if (actorDocs != null) {
            List<Personnel> actors = actorDocs.stream()
                    .map(MovieDao::fromSubDocument)
                    .toList();
            movie.setActors(actors);
        }

        return movie;
    }

    // TODO Consider refactoring into a mapper class
    private static Personnel fromSubDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        Personnel p = new Personnel();
        p.setFirstName(doc.getString("firstName"));
        p.setLastName(doc.getString("lastName"));
        return p;
    }
}
