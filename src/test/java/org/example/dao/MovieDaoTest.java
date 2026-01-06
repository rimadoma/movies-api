package org.example.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.model.Movie;
import org.example.model.Personnel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for regression testing of MovieDao. Best complemented with integration tests that test
 * app functionality end-to-end with a MongoDB instance (e.g. with Cucumber).
 */
public class MovieDaoTest {
    private MongoCollection<Document> mockCollection;
    private MovieDao movieDao;

    @BeforeEach
    void setup() {
        MongoClient client = stubClient();

        movieDao = new MovieDao(client);
    }

    // We know assigning MongoCollection to MongoCollection<Document> here is safe
    @SuppressWarnings("unchecked")
    private MongoClient stubClient() {
        final MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDb = mock(MongoDatabase.class);
        mockCollection = mock(MongoCollection.class);
        when(mockClient.getDatabase("harkka")).thenReturn(mockDb);
        when(mockDb.getCollection("movies")).thenReturn(mockCollection);
        return mockClient;
    }

    @Test
    void replaceMovie() {
        // Arrange
        Movie movie = new Movie()
                .name("Naked Gun")
                .year(1982)
                .ageLimit(16)
                .rating(5)
                .synopsis("A comedy classic");
        Personnel director = new Personnel()
                .firstName("David")
                .lastName("Zucker");
        movie.setDirector(director);

        Document expectedDoc = new Document()
                .append("name", "Naked Gun")
                .append("year", 1982)
                .append("ageLimit", 16)
                .append("rating", 5)
                .append("synopsis", "A comedy classic")
                .append("director",
                        new Document()
                                .append("firstName", "David")
                                .append("lastName", "Zucker")
                );

        Bson expectedFilter = Filters.and(
                Filters.eq("name", "Naked Gun"),
                Filters.eq("year", 1982)
        );

        // Act
        movieDao.replaceMovie(movie);

        // Assert
        ArgumentCaptor<Bson> filterCaptor = ArgumentCaptor.forClass(Bson.class);
        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        ArgumentCaptor<ReplaceOptions> optionsCaptor =
                ArgumentCaptor.forClass(ReplaceOptions.class);
        verify(mockCollection).replaceOne(
                filterCaptor.capture(),
                docCaptor.capture(),
                optionsCaptor.capture()
        );
        assertEquals(expectedFilter.toBsonDocument().toJson(),
                filterCaptor.getValue().toBsonDocument().toJson());
        assertEquals(expectedDoc.toJson(), docCaptor.getValue().toJson());
        assertTrue(optionsCaptor.getValue().isUpsert());
    }

    @Test
    void findAll() {
        // Arrange
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockCollection.find()).thenReturn(mockFindIterable);
        List<Document> movieDocs = List.of(
                new Document()
                        .append("name", "A")
                        .append("year", 2000),

                new Document()
                        .append("name", "B")
                        .append("year", 2001)
        );
        when(mockFindIterable.into(any())).thenReturn(movieDocs);

        List<Movie> expectedMovies = List.of(
                new Movie().name("A").year(2000),
                new Movie().name("B").year(2001)
        );

        // Act
        List<Movie> result = movieDao.findAll();

        // Assert
        assertEquals(expectedMovies, result);
    }
}